package com.nexeum.client.ui;

import com.nexeum.client.config.HudPosition;
import com.nexeum.client.config.NexeumConfig;
import com.nexeum.client.config.NexeumConfigManager;
import com.nexeum.client.pvp.NexeumMaceHelper;
import com.nexeum.client.runtime.NexeumRuntime;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.EntityHitResult;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class NexeumHudRenderer {
	private NexeumHudRenderer() {
	}

	public static void render(GuiGraphics graphics, DeltaTracker tickCounter) {
		Minecraft client = Minecraft.getInstance();

		if (client.player == null || client.options.hideGui) {
			return;
		}

		NexeumConfig config = NexeumConfigManager.getConfig();
		int width = graphics.guiWidth();
		int height = graphics.guiHeight();

		if (config.nebulaOverlayEnabled) {
			NexeumTheme.renderHudNebula(graphics, width, height, System.currentTimeMillis());
		}

		if (config.starCrosshairEnabled) {
			renderStarCrosshair(graphics, width / 2, height / 2);
		}

		if (config.cooldownMeterEnabled) {
			renderModule(graphics, client, config, NexeumHudModule.COOLDOWN_METER, false);
		}

		if (config.targetHudEnabled && getTargetEntity(client) != null) {
			renderModule(graphics, client, config, NexeumHudModule.TARGET_HUD, false);
		}

		if (config.maceDamageHudEnabled && shouldRenderMaceDamageHud(client)) {
			renderModule(graphics, client, config, NexeumHudModule.MACE_DAMAGE, false);
		}

		if (config.infoHudEnabled || config.cpsHudEnabled) {
			renderModule(graphics, client, config, NexeumHudModule.INFO_PANEL, false);
		}

		if (config.keystrokesHudEnabled) {
			renderModule(graphics, client, config, NexeumHudModule.KEYSTROKES, false);
		}

		if (config.armorHudEnabled && !getArmorStacks(client, false).isEmpty()) {
			renderModule(graphics, client, config, NexeumHudModule.ARMOR_HUD, false);
		}
	}

	public static List<NexeumHudModule> getMovableModules() {
		return List.of(NexeumHudModule.values());
	}

	public static HudBounds getModuleBounds(Minecraft client, NexeumConfig config, NexeumHudModule module, int guiWidth, int guiHeight, boolean preview) {
		int[] size = measureModule(client, config, module, preview);
		HudPosition position = getPosition(config, module);
		int maxX = Math.max(guiWidth - size[0], 0);
		int maxY = Math.max(guiHeight - size[1], 0);
		int x = Math.max(0, Math.min(maxX, (int) Math.round(maxX * clamp01(position.x))));
		int y = Math.max(0, Math.min(maxY, (int) Math.round(maxY * clamp01(position.y))));
		return new HudBounds(x, y, size[0], size[1]);
	}

	public static void moveModule(NexeumConfig config, Minecraft client, NexeumHudModule module, int guiWidth, int guiHeight, int x, int y) {
		HudBounds bounds = getModuleBounds(client, config, module, guiWidth, guiHeight, true);
		HudPosition position = getPosition(config, module);
		int maxX = Math.max(guiWidth - bounds.width(), 0);
		int maxY = Math.max(guiHeight - bounds.height(), 0);

		position.x = maxX == 0 ? 0.0D : clamp01(x / (double) maxX);
		position.y = maxY == 0 ? 0.0D : clamp01(y / (double) maxY);
	}

	public static void nudgeModule(NexeumConfig config, Minecraft client, NexeumHudModule module, int guiWidth, int guiHeight, int deltaX, int deltaY) {
		HudBounds bounds = getModuleBounds(client, config, module, guiWidth, guiHeight, true);
		moveModule(config, client, module, guiWidth, guiHeight, bounds.x() + deltaX, bounds.y() + deltaY);
	}

	public static boolean supportsLayoutToggle(NexeumHudModule module) {
		return module == NexeumHudModule.ARMOR_HUD;
	}

	public static boolean isVerticalLayout(NexeumConfig config, NexeumHudModule module) {
		return module == NexeumHudModule.ARMOR_HUD && config.armorHudVertical;
	}

	public static void toggleLayout(NexeumConfig config, Minecraft client, NexeumHudModule module, int guiWidth, int guiHeight) {
		if (module == NexeumHudModule.ARMOR_HUD) {
			HudBounds currentBounds = getModuleBounds(client, config, module, guiWidth, guiHeight, true);
			config.armorHudVertical = !config.armorHudVertical;
			moveModule(config, client, module, guiWidth, guiHeight, currentBounds.x(), currentBounds.y());
		}
	}

	public static void renderEditorPreview(GuiGraphics graphics, Minecraft client, NexeumConfig config, NexeumHudModule module) {
		renderModule(graphics, client, config, module, true);
	}

	private static void renderModule(GuiGraphics graphics, Minecraft client, NexeumConfig config, NexeumHudModule module, boolean preview) {
		HudBounds bounds = getModuleBounds(client, config, module, graphics.guiWidth(), graphics.guiHeight(), preview);

		switch (module) {
			case TARGET_HUD -> renderTargetHud(graphics, client, bounds.x(), bounds.y(), preview);
			case COOLDOWN_METER -> renderCooldownMeter(graphics, client, bounds.x(), bounds.y(), preview);
			case MACE_DAMAGE -> renderMaceDamageHud(graphics, client, bounds.x(), bounds.y(), preview);
			case INFO_PANEL -> renderInfoPanel(graphics, client, config, bounds.x(), bounds.y(), preview);
			case KEYSTROKES -> renderKeystrokes(graphics, client, bounds.x(), bounds.y(), preview);
			case ARMOR_HUD -> renderArmorHud(graphics, client, bounds.x(), bounds.y(), preview);
		}
	}

	private static void renderStarCrosshair(GuiGraphics graphics, int centerX, int centerY) {
		int color = NexeumTheme.AQUA;
		graphics.fill(centerX - 1, centerY - 5, centerX + 1, centerY - 1, color);
		graphics.fill(centerX - 1, centerY + 1, centerX + 1, centerY + 5, color);
		graphics.fill(centerX - 5, centerY - 1, centerX - 1, centerY + 1, color);
		graphics.fill(centerX + 1, centerY - 1, centerX + 5, centerY + 1, color);
		graphics.fill(centerX - 3, centerY - 3, centerX - 2, centerY - 2, color);
		graphics.fill(centerX + 2, centerY - 3, centerX + 3, centerY - 2, color);
		graphics.fill(centerX - 3, centerY + 2, centerX - 2, centerY + 3, color);
		graphics.fill(centerX + 2, centerY + 2, centerX + 3, centerY + 3, color);
		graphics.fill(centerX, centerY, centerX + 1, centerY + 1, NexeumTheme.GOLD);
	}

	private static void renderCooldownMeter(GuiGraphics graphics, Minecraft client, int x, int y, boolean preview) {
		float progress = preview ? 0.84F : client.player.getAttackStrengthScale(0.0F);
		int filled = Math.max(0, Math.min(34, Math.round(progress * 34.0F)));

		graphics.fill(x, y, x + 36, y + 6, 0xA0101622);
		graphics.fill(x + 1, y + 1, x + 35, y + 5, 0x70213143);
		graphics.fillGradient(x + 1, y + 1, x + 1 + filled, y + 5, NexeumTheme.AQUA, NexeumTheme.GOLD);

		if (progress > 0.98F) {
			graphics.fill(x + 17, y - 3, x + 19, y - 1, NexeumTheme.GOLD);
		}
	}

	private static void renderTargetHud(GuiGraphics graphics, Minecraft client, int x, int y, boolean preview) {
		LivingEntity target = getTargetEntity(client);

		if (!preview && target == null) {
			return;
		}

		Font font = client.font;
		NexeumTheme.renderPanel(graphics, x, y, 168, 50);

		Component name = target != null ? target.getDisplayName() : Component.translatable("nexeum.editor.preview.target");
		float maxHealth = target != null ? Math.max(1.0F, target.getMaxHealth() + target.getAbsorptionAmount()) : 20.0F;
		float shownHealth = target != null ? Math.max(0.0F, target.getHealth() + target.getAbsorptionAmount()) : 18.0F;
		float distance = target != null && client.player != null ? client.player.distanceTo(target) : 3.4F;
		int combo = target != null ? NexeumRuntime.getComboCountFor(target) : 3;

		graphics.drawString(font, name, x + 10, y + 8, NexeumTheme.PANEL_TEXT);

		int filled = Math.max(0, Math.min(112, Math.round((shownHealth / maxHealth) * 112.0F)));
		graphics.fill(x + 10, y + 21, x + 122, y + 27, 0x80303B4C);
		graphics.fillGradient(x + 10, y + 21, x + 10 + filled, y + 27, NexeumTheme.AQUA, NexeumTheme.GOLD);

		graphics.drawString(
			font,
			Component.translatable("nexeum.hud.health", formatTenth(shownHealth / 2.0F), formatTenth(maxHealth / 2.0F)),
			x + 10,
			y + 32,
			NexeumTheme.SUBTLE_TEXT
		);
		graphics.drawString(font, Component.translatable("nexeum.hud.distance", formatTenth(distance)), x + 92, y + 32, 0xFFBFD6F2);

		if (combo > 1) {
			graphics.drawString(font, Component.translatable("nexeum.hud.combo", Integer.toString(combo)), x + 108, y + 8, NexeumTheme.GOLD);
		}
	}

	private static void renderArmorHud(GuiGraphics graphics, Minecraft client, int x, int y, boolean preview) {
		List<ItemStack> armor = getArmorStacks(client, preview);

		if (armor.isEmpty()) {
			return;
		}

		Font font = client.font;
		boolean vertical = NexeumConfigManager.getConfig().armorHudVertical;
		int currentX = x;
		int currentY = y;

		for (ItemStack stack : armor) {
			graphics.renderItem(stack, currentX, currentY);
			graphics.renderItemDecorations(font, stack, currentX, currentY);

			if (stack.isDamageableItem()) {
				int remaining = stack.getMaxDamage() - stack.getDamageValue();
				int percent = Math.max(0, Math.min(100, Math.round(remaining * 100.0F / stack.getMaxDamage())));
				String text = Integer.toString(percent);

				if (vertical) {
					graphics.drawString(font, text, currentX + 22, currentY + 5, 0xFFD8F2FF);
				} else {
					int textX = currentX + 8 - font.width(text) / 2;
					graphics.drawString(font, text, textX, currentY + 18, 0xFFD8F2FF);
				}
			}

			if (vertical) {
				currentY += 20;
			} else {
				currentX += 20;
			}
		}
	}

	private static void renderInfoPanel(GuiGraphics graphics, Minecraft client, NexeumConfig config, int x, int y, boolean preview) {
		Font font = client.font;
		List<Component> lines = getInfoPanelLines(client, config, preview);

		if (lines.isEmpty()) {
			return;
		}

		int maxWidth = 0;
		for (Component line : lines) {
			maxWidth = Math.max(maxWidth, font.width(line));
		}

		int panelWidth = maxWidth + 16;
		int panelHeight = lines.size() * 11 + 10;
		NexeumTheme.renderPanel(graphics, x, y, panelWidth, panelHeight);

		for (int index = 0; index < lines.size(); index++) {
			graphics.drawString(font, lines.get(index), x + 8, y + 6 + index * 11, NexeumTheme.PANEL_TEXT);
		}
	}

	private static void renderMaceDamageHud(GuiGraphics graphics, Minecraft client, int x, int y, boolean preview) {
		Font font = client.font;
		List<Component> lines = getMaceDamageLines(client, preview);

		if (lines.isEmpty()) {
			return;
		}

		int[] size = measureTextPanel(font, lines, 114, 32);
		NexeumTheme.renderPanel(graphics, x, y, size[0], size[1]);

		for (int index = 0; index < lines.size(); index++) {
			int color = index == 0 ? NexeumTheme.GOLD : NexeumTheme.PANEL_TEXT;
			graphics.drawString(font, lines.get(index), x + 8, y + 6 + index * 11, color);
		}
	}

	private static void renderKeystrokes(GuiGraphics graphics, Minecraft client, int x, int y, boolean preview) {
		int cell = 18;
		int gap = 3;
		boolean up = preview || client.options.keyUp.isDown();
		boolean left = preview && client.player == null ? false : client.options.keyLeft.isDown();
		boolean down = preview && client.player == null ? false : client.options.keyDown.isDown();
		boolean right = preview || client.options.keyRight.isDown();
		boolean attack = preview || client.options.keyAttack.isDown();
		boolean use = preview && client.player == null ? false : client.options.keyUse.isDown();

		NexeumTheme.renderPanel(graphics, x, y, 74, 74);
		graphics.drawString(client.font, Component.translatable("nexeum.hud.keys"), x + 5, y + 6, NexeumTheme.SUBTLE_TEXT);

		renderKeyCell(graphics, client, x + 24, y + 18, cell, "W", up);
		renderKeyCell(graphics, client, x + 3, y + 39, cell, "A", left);
		renderKeyCell(graphics, client, x + 24, y + 39, cell, "S", down);
		renderKeyCell(graphics, client, x + 45, y + 39, cell, "D", right);
		renderWideKeyCell(graphics, client, x + 3, y + 60, 30, "LMB", attack);
		renderWideKeyCell(graphics, client, x + 36, y + 60, 30, "RMB", use);
	}

	private static void renderKeyCell(GuiGraphics graphics, Minecraft client, int x, int y, int size, String label, boolean pressed) {
		int fill = pressed ? 0xD0305D8B : 0xB0141C2D;
		graphics.fillGradient(x, y, x + size, y + size, fill, fill);
		graphics.fill(x, y, x + size, y + 1, pressed ? NexeumTheme.GOLD : NexeumTheme.AQUA);
		int labelX = x + size / 2 - client.font.width(label) / 2;
		graphics.drawString(client.font, label, labelX, y + 5, NexeumTheme.PANEL_TEXT);
	}

	private static void renderWideKeyCell(GuiGraphics graphics, Minecraft client, int x, int y, int width, String label, boolean pressed) {
		int fill = pressed ? 0xD0305D8B : 0xB0141C2D;
		graphics.fillGradient(x, y, x + width, y + 18, fill, fill);
		graphics.fill(x, y, x + width, y + 1, pressed ? NexeumTheme.GOLD : NexeumTheme.AQUA);
		int labelX = x + width / 2 - client.font.width(label) / 2;
		graphics.drawString(client.font, label, labelX, y + 5, NexeumTheme.PANEL_TEXT);
	}

	private static int[] measureModule(Minecraft client, NexeumConfig config, NexeumHudModule module, boolean preview) {
		return switch (module) {
			case TARGET_HUD -> new int[]{168, 50};
			case COOLDOWN_METER -> new int[]{36, 8};
			case MACE_DAMAGE -> measureMaceDamageHud(client, preview);
			case INFO_PANEL -> measureInfoPanel(client, config, preview);
			case KEYSTROKES -> new int[]{74, 74};
			case ARMOR_HUD -> measureArmorHud(client, config, preview);
		};
	}

	private static int[] measureMaceDamageHud(Minecraft client, boolean preview) {
		return measureTextPanel(client.font, getMaceDamageLines(client, preview), 114, 32);
	}

	private static int[] measureInfoPanel(Minecraft client, NexeumConfig config, boolean preview) {
		Font font = client.font;
		List<Component> lines = getInfoPanelLines(client, config, preview);
		int maxWidth = 0;

		for (Component line : lines) {
			maxWidth = Math.max(maxWidth, font.width(line));
		}

		return new int[]{Math.max(96, maxWidth + 16), Math.max(21, lines.size() * 11 + 10)};
	}

	private static List<Component> getInfoPanelLines(Minecraft client, NexeumConfig config, boolean preview) {
		List<Component> lines = new ArrayList<>();
		boolean includeInfo = preview || config.infoHudEnabled;
		boolean includeCps = preview || config.cpsHudEnabled;

		if (includeInfo) {
			if (client.player != null) {
				PlayerInfo playerInfo = client.player.connection.getPlayerInfo(client.player.getUUID());
				int ping = playerInfo != null ? playerInfo.getLatency() : 0;
				lines.add(Component.translatable("nexeum.hud.fps", Integer.toString(client.getFps())));
				lines.add(Component.translatable("nexeum.hud.ping", Integer.toString(ping)));
				lines.add(Component.translatable(
					"nexeum.hud.xyz",
					formatCoord(client.player.getX()),
					formatCoord(client.player.getY()),
					formatCoord(client.player.getZ())
				));
			} else {
				lines.add(Component.translatable("nexeum.hud.fps", "144"));
				lines.add(Component.translatable("nexeum.hud.ping", "32"));
				lines.add(Component.translatable("nexeum.hud.xyz", "120", "64", "-48"));
			}
		}

		if (includeCps) {
			if (preview) {
				lines.add(Component.translatable("nexeum.hud.cps", "12", "8"));
			} else {
				lines.add(Component.translatable(
					"nexeum.hud.cps",
					Integer.toString(NexeumRuntime.getLeftCps()),
					Integer.toString(NexeumRuntime.getRightCps())
				));
			}
		}

		return lines;
	}

	private static List<Component> getMaceDamageLines(Minecraft client, boolean preview) {
		if (preview || client.player == null) {
			return List.of(
				Component.translatable("nexeum.hud.mace.title"),
				Component.translatable("nexeum.hud.mace.ready"),
				Component.translatable("nexeum.hud.mace.estimate", "24.0"),
				Component.translatable("nexeum.hud.mace.bonus", "18.0"),
				Component.translatable("nexeum.hud.mace.fall", "4.5", "100")
			);
		}

		NexeumMaceHelper.MaceEstimate liveEstimate = NexeumMaceHelper.getEstimate(client.player);
		NexeumRuntime.MaceHitSample recentEstimate = NexeumRuntime.getRecentMaceEstimate();

		if (!liveEstimate.holdingMace()) {
			if (recentEstimate == null) {
				return List.of();
			}

			return List.of(
				Component.translatable("nexeum.hud.mace.title"),
				Component.translatable("nexeum.hud.mace.recent"),
				Component.translatable("nexeum.hud.mace.last", formatTenth(recentEstimate.estimatedDamage())),
				Component.translatable("nexeum.hud.mace.bonus", formatTenth(recentEstimate.bonusDamage())),
				Component.translatable("nexeum.hud.mace.fall_only", formatTenth(recentEstimate.fallDistance()))
			);
		}

		List<Component> lines = new ArrayList<>();
		lines.add(Component.translatable("nexeum.hud.mace.title"));
		lines.add(Component.translatable(liveEstimate.smashReady() ? "nexeum.hud.mace.ready" : "nexeum.hud.mace.need_fall"));
		lines.add(Component.translatable("nexeum.hud.mace.estimate", formatTenth(liveEstimate.estimatedDamage())));
		lines.add(Component.translatable("nexeum.hud.mace.bonus", formatTenth(liveEstimate.bonusDamage())));
		lines.add(Component.translatable("nexeum.hud.mace.fall", formatTenth(liveEstimate.fallDistance()), Integer.toString(liveEstimate.chargePercent())));

		if (recentEstimate != null) {
			lines.add(Component.translatable("nexeum.hud.mace.last", formatTenth(recentEstimate.estimatedDamage())));
		}

		return lines;
	}

	private static List<ItemStack> getArmorStacks(Minecraft client, boolean preview) {
		List<ItemStack> armor = new ArrayList<>();

		if (client.player != null) {
			for (ItemStack stack : client.player.getArmorSlots()) {
				if (!stack.isEmpty()) {
					armor.add(stack);
				}
			}
		}

		if (armor.isEmpty() && preview) {
			armor.add(new ItemStack(Items.DIAMOND_BOOTS));
			armor.add(new ItemStack(Items.DIAMOND_LEGGINGS));
			armor.add(new ItemStack(Items.DIAMOND_CHESTPLATE));
			armor.add(new ItemStack(Items.DIAMOND_HELMET));
		}

		Collections.reverse(armor);
		return armor;
	}

	private static int[] measureArmorHud(Minecraft client, NexeumConfig config, boolean preview) {
		List<ItemStack> armor = getArmorStacks(client, preview);
		if (config.armorHudVertical) {
			return new int[]{52, Math.max(20, armor.size() * 20)};
		}

		return new int[]{Math.max(80, armor.size() * 20), 38};
	}

	private static int[] measureTextPanel(Font font, List<Component> lines, int minWidth, int minHeight) {
		int maxWidth = 0;

		for (Component line : lines) {
			maxWidth = Math.max(maxWidth, font.width(line));
		}

		return new int[]{Math.max(minWidth, maxWidth + 16), Math.max(minHeight, lines.size() * 11 + 10)};
	}

	private static LivingEntity getTargetEntity(Minecraft client) {
		if (client.hitResult instanceof EntityHitResult hitResult && hitResult.getEntity() instanceof LivingEntity target && target != client.player) {
			return target;
		}

		return null;
	}

	private static HudPosition getPosition(NexeumConfig config, NexeumHudModule module) {
		return switch (module) {
			case TARGET_HUD -> config.targetHudPosition;
			case COOLDOWN_METER -> config.cooldownMeterPosition;
			case MACE_DAMAGE -> config.maceDamageHudPosition;
			case INFO_PANEL -> config.infoPanelPosition;
			case KEYSTROKES -> config.keystrokesPosition;
			case ARMOR_HUD -> config.armorHudPosition;
		};
	}

	private static boolean shouldRenderMaceDamageHud(Minecraft client) {
		return client.player != null && NexeumMaceHelper.isHoldingMace(client.player) || NexeumRuntime.getRecentMaceEstimate() != null;
	}

	private static double clamp01(double value) {
		return Math.max(0.0D, Math.min(1.0D, value));
	}

	private static String formatTenth(float value) {
		int scaled = Math.round(value * 10.0F);
		int whole = scaled / 10;
		int decimal = Math.abs(scaled % 10);
		return whole + "." + decimal;
	}

	private static String formatCoord(double value) {
		return Integer.toString((int) Math.floor(value));
	}

	public record HudBounds(int x, int y, int width, int height) {
	}
}
