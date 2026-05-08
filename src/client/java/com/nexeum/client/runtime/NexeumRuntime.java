package com.nexeum.client.runtime;

import com.nexeum.client.config.NexeumConfig;
import com.nexeum.client.config.NexeumConfigManager;
import com.nexeum.client.pvp.NexeumMaceHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayDeque;
import java.util.UUID;

public final class NexeumRuntime {
	private static final long COMBO_TIMEOUT_MS = 1_300L;
	private static final long CPS_WINDOW_MS = 1_000L;
	private static final long MACE_ESTIMATE_WINDOW_MS = 2_500L;
	private static final double FULLBRIGHT_GAMMA = 1.0D;

	private static boolean lastAttackDown;
	private static boolean lastUseDown;
	private static boolean zoomApplied;
	private static boolean fullbrightApplied;
	private static UUID comboTargetId;
	private static int comboCount;
	private static int zoomPreviousFov;
	private static long lastComboTime;
	private static long lastMaceEstimateTime;
	private static double gammaBeforeFullbright;
	private static final ArrayDeque<Long> leftClickTimes = new ArrayDeque<>();
	private static final ArrayDeque<Long> rightClickTimes = new ArrayDeque<>();
	private static MaceHitSample lastMaceEstimate;

	private NexeumRuntime() {
	}

	public static void tick(Minecraft client, boolean zoomHeld) {
		if (client.options == null) {
			return;
		}

		boolean attackDown = client.options.keyAttack.isDown();
		boolean useDown = client.options.keyUse.isDown();
		NexeumConfig config = NexeumConfigManager.getConfig();

		applyFullbright(client, config.fullbrightEnabled);
		applyZoom(client, config.zoomEnabled && zoomHeld && client.screen == null);

		if (client.player == null || client.level == null || client.isPaused()) {
			lastAttackDown = attackDown;
			lastUseDown = useDown;
			expireCombo();
			return;
		}

		if (config.autoSprintEnabled) {
			applyAutoSprint(client);
		}

		if (attackDown && !lastAttackDown) {
			recordClick(leftClickTimes);
			handleAttackClick(client);
		}

		if (useDown && !lastUseDown) {
			recordClick(rightClickTimes);
		}

		lastAttackDown = attackDown;
		lastUseDown = useDown;

		if (config.starHaloEnabled) {
			spawnStarHalo(client);
		}

		if (config.cometTrailEnabled) {
			spawnCometTrail(client);
		}

		expireCombo();
	}

	public static void restoreTransientOptions(Minecraft client) {
		restoreZoom(client);
		restoreFullbright(client);
	}

	public static int getComboCountFor(Entity entity) {
		if (entity == null || comboCount <= 1 || comboTargetId == null) {
			return 0;
		}

		return comboTargetId.equals(entity.getUUID()) ? comboCount : 0;
	}

	public static int getLeftCps() {
		pruneClicks(leftClickTimes, System.currentTimeMillis());
		return leftClickTimes.size();
	}

	public static int getRightCps() {
		pruneClicks(rightClickTimes, System.currentTimeMillis());
		return rightClickTimes.size();
	}

	public static MaceHitSample getRecentMaceEstimate() {
		if (lastMaceEstimate == null) {
			return null;
		}

		if (System.currentTimeMillis() - lastMaceEstimateTime > MACE_ESTIMATE_WINDOW_MS) {
			lastMaceEstimate = null;
			return null;
		}

		return lastMaceEstimate;
	}

	private static void applyAutoSprint(Minecraft client) {
		if (!client.options.keyUp.isDown()) {
			return;
		}

		if (client.player.isUsingItem() || client.player.isCrouching() || client.player.horizontalCollision || !client.player.canSprint()) {
			return;
		}

		client.player.setSprinting(true);
	}

	private static void applyFullbright(Minecraft client, boolean enabled) {
		if (!enabled) {
			restoreFullbright(client);
			return;
		}

		double currentGamma = client.options.gamma().get();

		if (!fullbrightApplied) {
			fullbrightApplied = true;
			gammaBeforeFullbright = currentGamma;
		}

		if (Math.abs(currentGamma - FULLBRIGHT_GAMMA) > 0.0001D) {
			client.options.gamma().set(FULLBRIGHT_GAMMA);
		}
	}

	private static void restoreFullbright(Minecraft client) {
		if (!fullbrightApplied) {
			return;
		}

		client.options.gamma().set(gammaBeforeFullbright);
		fullbrightApplied = false;
	}

	private static void applyZoom(Minecraft client, boolean enabled) {
		if (!enabled) {
			restoreZoom(client);
			return;
		}

		int currentFov = client.options.fov().get();

		if (!zoomApplied) {
			zoomApplied = true;
			zoomPreviousFov = currentFov;
		}

		int zoomFov = Math.max(10, Math.round(zoomPreviousFov * 0.35F));
		if (currentFov != zoomFov) {
			client.options.fov().set(zoomFov);
		}
	}

	private static void restoreZoom(Minecraft client) {
		if (!zoomApplied) {
			return;
		}

		client.options.fov().set(zoomPreviousFov);
		zoomApplied = false;
	}

	private static void handleAttackClick(Minecraft client) {
		recordMaceEstimate(client);

		if (!(client.hitResult instanceof EntityHitResult hitResult)) {
			return;
		}

		if (!(hitResult.getEntity() instanceof LivingEntity target)) {
			return;
		}

		long now = System.currentTimeMillis();

		if (target.getUUID().equals(comboTargetId) && now - lastComboTime <= COMBO_TIMEOUT_MS) {
			comboCount++;
		} else {
			comboTargetId = target.getUUID();
			comboCount = 1;
		}

		lastComboTime = now;
	}

	private static void recordMaceEstimate(Minecraft client) {
		if (client.player == null) {
			return;
		}

		NexeumMaceHelper.MaceEstimate estimate = NexeumMaceHelper.getEstimate(client.player);
		if (!estimate.holdingMace()) {
			return;
		}

		lastMaceEstimate = new MaceHitSample(estimate.estimatedDamage(), estimate.bonusDamage(), estimate.fallDistance());
		lastMaceEstimateTime = System.currentTimeMillis();
	}

	private static void expireCombo() {
		if (comboCount <= 0) {
			return;
		}

		if (System.currentTimeMillis() - lastComboTime > COMBO_TIMEOUT_MS) {
			comboCount = 0;
			comboTargetId = null;
		}
	}

	private static void recordClick(ArrayDeque<Long> clickTimes) {
		long now = System.currentTimeMillis();
		clickTimes.addLast(now);
		pruneClicks(clickTimes, now);
	}

	private static void pruneClicks(ArrayDeque<Long> clickTimes, long now) {
		while (!clickTimes.isEmpty() && now - clickTimes.peekFirst() > CPS_WINDOW_MS) {
			clickTimes.removeFirst();
		}
	}

	private static void spawnStarHalo(Minecraft client) {
		if (client.player.tickCount % 3 != 0) {
			return;
		}

		double baseAngle = client.player.tickCount * 0.17D;
		double centerX = client.player.getX();
		double centerY = client.player.getY() + client.player.getBbHeight() + 0.15D;
		double centerZ = client.player.getZ();

		for (int index = 0; index < 5; index++) {
			double angle = baseAngle + index * (Math.PI * 2.0D / 5.0D);
			double radius = 0.55D + (index % 2) * 0.05D;
			double x = centerX + Math.cos(angle) * radius;
			double y = centerY + Math.sin(baseAngle * 1.6D + index) * 0.05D;
			double z = centerZ + Math.sin(angle) * radius;

			client.level.addParticle(ParticleTypes.END_ROD, x, y, z, 0.0D, 0.01D, 0.0D);
		}
	}

	private static void spawnCometTrail(Minecraft client) {
		Vec3 movement = client.player.getDeltaMovement();

		if (movement.horizontalDistanceSqr() < 0.0025D || client.player.tickCount % 2 != 0) {
			return;
		}

		Vec3 direction = movement.normalize();
		Vec3 trailBase = client.player.position()
			.subtract(direction.scale(0.45D))
			.add(0.0D, 0.18D, 0.0D);

		client.level.addParticle(
			ParticleTypes.END_ROD,
			trailBase.x,
			trailBase.y,
			trailBase.z,
			-direction.x * 0.02D,
			0.01D,
			-direction.z * 0.02D
		);
		client.level.addParticle(
			ParticleTypes.ENCHANT,
			trailBase.x,
			trailBase.y + 0.08D,
			trailBase.z,
			-direction.x * 0.04D,
			0.0D,
			-direction.z * 0.04D
		);
	}

	public record MaceHitSample(float estimatedDamage, float bonusDamage, float fallDistance) {
	}
}
