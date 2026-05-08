package com.nexeum.client.pvp;

import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.MaceItem;

public final class NexeumMaceHelper {
	private NexeumMaceHelper() {
	}

	public static MaceEstimate getEstimate(Player player) {
		boolean holdingMace = isHoldingMace(player);
		float fallDistance = Math.max(0.0F, player.fallDistance);
		boolean smashReady = holdingMace && fallDistance > 1.5F && !player.isFallFlying();
		float bonusDamage = smashReady ? calculateSmashBonus(fallDistance) : 0.0F;
		float attackCharge = Math.max(0.0F, Math.min(1.0F, player.getAttackStrengthScale(0.0F)));
		float chargeScale = 0.2F + attackCharge * attackCharge * 0.8F;
		float baseDamage = (float) player.getAttributeValue(Attributes.ATTACK_DAMAGE);
		float estimatedDamage = holdingMace ? baseDamage * chargeScale + bonusDamage : 0.0F;
		int chargePercent = Math.max(0, Math.min(100, Math.round(attackCharge * 100.0F)));
		return new MaceEstimate(holdingMace, smashReady, estimatedDamage, bonusDamage, fallDistance, chargePercent);
	}

	public static boolean isHoldingMace(Player player) {
		return player.getMainHandItem().getItem() instanceof MaceItem;
	}

	public static float calculateSmashBonus(float fallDistance) {
		if (fallDistance <= 3.0F) {
			return 4.0F * fallDistance;
		}

		if (fallDistance <= 8.0F) {
			return 12.0F + 2.0F * (fallDistance - 3.0F);
		}

		return fallDistance + 14.0F;
	}

	public record MaceEstimate(
		boolean holdingMace,
		boolean smashReady,
		float estimatedDamage,
		float bonusDamage,
		float fallDistance,
		int chargePercent
	) {
	}
}
