package com.nexeum.client.ui;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

public final class NexeumTheme {
	public static final int PANEL_TEXT = 0xFFEAF7FF;
	public static final int SUBTLE_TEXT = 0xFF95B4D3;
	public static final int AQUA = 0xFF75E6FF;
	public static final int GOLD = 0xFFFFD978;

	private NexeumTheme() {
	}

	public static void renderScreenBackground(GuiGraphics graphics, int width, int height, long timeMs) {
		graphics.fillGradient(0, 0, width, height, 0xFF070B17, 0xFF140A26);
		graphics.fillGradient(0, 0, width, height / 2, 0x440F2E57, 0x00000000);
		graphics.fillGradient(0, height / 3, width, height, 0x00000000, 0x66071321);
		drawStars(graphics, width, height, timeMs, 44, 0);
	}

	public static void renderHudNebula(GuiGraphics graphics, int width, int height, long timeMs) {
		graphics.fillGradient(0, 0, width / 3, 36, 0x40125B7D, 0x00000000);
		graphics.fillGradient(width * 2 / 3, 0, width, 36, 0x00000000, 0x401F3F79);
		drawStars(graphics, width, 28, timeMs, 18, 7);
	}

	public static void renderTitleOverlay(GuiGraphics graphics, int width, int height, long timeMs) {
		graphics.fillGradient(0, 0, width, height, 0x7A050A14, 0xB0080612);
		graphics.fillGradient(0, 0, width, height / 2, 0x501B4F80, 0x00000000);
		graphics.fillGradient(0, height / 2, width, height, 0x00000000, 0x70110A22);
		graphics.fill(0, height - 88, width, height, 0x35050B18);
		drawStars(graphics, width, height, timeMs, 68, 11);
	}

	public static void renderPanel(GuiGraphics graphics, int x, int y, int width, int height) {
		graphics.fillGradient(x, y, x + width, y + height, 0xD01A2036, 0xD0121627);
		graphics.fillGradient(x, y, x + width, y + 18, 0x702B5E8E, 0x00122A3F);
		graphics.fill(x, y, x + width, y + 1, AQUA);
		graphics.fill(x, y + height - 1, x + width, y + height, 0xFF1B3856);
		graphics.fill(x, y, x + 1, y + height, 0xFF214C74);
		graphics.fill(x + width - 1, y, x + width, y + height, 0xFF214C74);
	}

	public static Component toggleState(boolean enabled) {
		return Component.translatable(enabled ? "nexeum.toggle.on" : "nexeum.toggle.off")
			.withStyle(enabled ? ChatFormatting.AQUA : ChatFormatting.DARK_GRAY, ChatFormatting.BOLD);
	}

	public static void renderChip(GuiGraphics graphics, Font font, int x, int y, Component text) {
		int width = font.width(text) + 12;
		graphics.fillGradient(x, y, x + width, y + 14, 0xA017233A, 0xA00F1728);
		graphics.fill(x, y, x + width, y + 1, AQUA);
		graphics.drawString(font, text, x + 6, y + 3, PANEL_TEXT);
	}

	private static void drawStars(GuiGraphics graphics, int width, int height, long timeMs, int count, int seedOffset) {
		for (int index = 0; index < count; index++) {
			int x = Math.floorMod(index * 73 + index * index * 11 + seedOffset * 29, Math.max(width, 1));
			int y = Math.floorMod(index * 41 + index * index * 7 + seedOffset * 13, Math.max(height, 1));
			int alpha = 95 + (int) (Math.sin(timeMs * 0.0022D + index * 0.8D) * 65.0D);
			alpha = Math.max(35, Math.min(alpha, 175));
			int size = index % 9 == 0 ? 2 : 1;
			graphics.fill(x, y, x + size, y + size, withAlpha(0xDFF8FF, alpha));
		}
	}

	private static int withAlpha(int rgb, int alpha) {
		return (alpha << 24) | (rgb & 0x00FFFFFF);
	}
}
