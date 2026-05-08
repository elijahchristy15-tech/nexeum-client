package com.nexeum.client.ui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;

public final class NexeumMenuButton extends Button {
	private final Component detail;
	private final int accentColor;

	public NexeumMenuButton(int x, int y, int width, int height, Component title, Component detail, int accentColor, OnPress onPress) {
		super(x, y, width, height, title, onPress, DEFAULT_NARRATION);
		this.detail = detail;
		this.accentColor = accentColor;
	}

	@Override
	protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
		Minecraft client = Minecraft.getInstance();
		int x = getX();
		int y = getY();
		int right = x + getWidth();
		int bottom = y + getHeight();
		boolean highlighted = isHoveredOrFocused();
		int topColor = highlighted ? 0xF0283A59 : 0xD0182438;
		int bottomColor = highlighted ? 0xF0152033 : 0xD0101827;

		graphics.fillGradient(x, y, right, bottom, topColor, bottomColor);
		graphics.fill(x, y, right, y + 2, this.accentColor);
		graphics.fill(x, bottom - 1, right, bottom, 0xFF1F3550);
		graphics.fill(x, y, x + 1, bottom, 0xFF22496E);
		graphics.fill(right - 1, y, right, bottom, 0xFF22496E);
		graphics.drawString(client.font, getMessage(), x + 9, y + 4, NexeumTheme.PANEL_TEXT);
		graphics.drawString(client.font, this.detail, x + 9, y + 15, highlighted ? 0xFFE2F0FF : NexeumTheme.SUBTLE_TEXT);
	}
}
