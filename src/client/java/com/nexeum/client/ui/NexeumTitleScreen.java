package com.nexeum.client.ui;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.client.gui.screens.options.AccessibilityOptionsScreen;
import net.minecraft.client.gui.screens.options.LanguageSelectScreen;
import net.minecraft.client.gui.screens.options.OptionsScreen;
import net.minecraft.client.gui.screens.worldselection.SelectWorldScreen;
import net.minecraft.network.chat.Component;

public final class NexeumTitleScreen extends TitleScreen {
	public NexeumTitleScreen() {
		super();
	}

	@Override
	protected void init() {
		super.init();
		this.clearWidgets();
		addMenuButtons();
	}

	@Override
	public void renderBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
		this.renderPanorama(graphics, partialTick);
		NexeumTheme.renderTitleOverlay(graphics, this.width, this.height, System.currentTimeMillis());
	}

	@Override
	public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
		super.render(graphics, mouseX, mouseY, partialTick);
		renderBranding(graphics);
	}

	@Override
	public boolean shouldCloseOnEsc() {
		return false;
	}

	@Override
	public void onClose() {
	}

	private void renderBranding(GuiGraphics graphics) {
		int bannerWidth = 252;
		int bannerHeight = 52;
		int bannerX = this.width / 2 - bannerWidth / 2;
		int bannerY = 14;

		NexeumTheme.renderPanel(graphics, bannerX, bannerY, bannerWidth, bannerHeight);
		graphics.drawCenteredString(this.font, Component.translatable("nexeum.menu.brand"), this.width / 2, bannerY + 8, NexeumTheme.PANEL_TEXT);
		graphics.drawCenteredString(this.font, Component.translatable("nexeum.menu.subtitle"), this.width / 2, bannerY + 22, NexeumTheme.SUBTLE_TEXT);
		graphics.drawCenteredString(this.font, Component.translatable("nexeum.menu.hint"), this.width / 2, bannerY + 35, NexeumTheme.GOLD);

		if (this.height >= 300) {
			int chipY = this.height - 34;
			int chipX = 10;
			NexeumTheme.renderChip(graphics, this.font, chipX, chipY, Component.translatable("nexeum.menu.chip.pvp"));
			NexeumTheme.renderChip(graphics, this.font, chipX + 84, chipY, Component.translatable("nexeum.menu.chip.hud"));
			NexeumTheme.renderChip(graphics, this.font, chipX + 152, chipY, Component.translatable("nexeum.menu.chip.cosmetic"));
		}
	}

	private void addMenuButtons() {
		int columnWidth = 172;
		int buttonHeight = 30;
		int gap = 4;
		int leftX = this.width / 2 - columnWidth - gap / 2;
		int rightX = this.width / 2 + gap / 2;
		int startY = 78;

		addRenderableWidget(new NexeumMenuButton(
			leftX,
			startY,
			columnWidth,
			buttonHeight,
			Component.translatable("nexeum.menu.button.worlds"),
			Component.translatable("nexeum.menu.button.worlds.detail"),
			NexeumTheme.AQUA,
			button -> this.minecraft.setScreen(new SelectWorldScreen(this))
		));
		addRenderableWidget(new NexeumMenuButton(
			rightX,
			startY,
			columnWidth,
			buttonHeight,
			Component.translatable("nexeum.menu.button.servers"),
			Component.translatable("nexeum.menu.button.servers.detail"),
			0xFF7ED6FF,
			button -> this.minecraft.setScreen(new JoinMultiplayerScreen(this))
		));
		addRenderableWidget(new NexeumMenuButton(
			leftX,
			startY + buttonHeight + gap,
			columnWidth,
			buttonHeight,
			Component.translatable("nexeum.menu.button.modules"),
			Component.translatable("nexeum.menu.button.modules.detail"),
			NexeumTheme.GOLD,
			button -> this.minecraft.setScreen(new NexeumConfigScreen(this, NexeumConfigScreen.Tab.MODULES))
		));
		addRenderableWidget(new NexeumMenuButton(
			rightX,
			startY + buttonHeight + gap,
			columnWidth,
			buttonHeight,
			Component.translatable("nexeum.menu.button.cosmetics"),
			Component.translatable("nexeum.menu.button.cosmetics.detail"),
			0xFFFFA7D5,
			button -> this.minecraft.setScreen(new NexeumConfigScreen(this, NexeumConfigScreen.Tab.COSMETICS))
		));
		addRenderableWidget(new NexeumMenuButton(
			leftX,
			startY + (buttonHeight + gap) * 2,
			columnWidth,
			buttonHeight,
			Component.translatable("nexeum.menu.button.options"),
			Component.translatable("nexeum.menu.button.options.detail"),
			0xFFA3C7FF,
			button -> this.minecraft.setScreen(new OptionsScreen(this, this.minecraft.options))
		));
		addRenderableWidget(new NexeumMenuButton(
			rightX,
			startY + (buttonHeight + gap) * 2,
			columnWidth,
			buttonHeight,
			Component.translatable("nexeum.menu.button.language"),
			Component.translatable("nexeum.menu.button.language.detail"),
			0xFF94F0CF,
			button -> this.minecraft.setScreen(new LanguageSelectScreen(this, this.minecraft.options, this.minecraft.getLanguageManager()))
		));
		addRenderableWidget(new NexeumMenuButton(
			leftX,
			startY + (buttonHeight + gap) * 3,
			columnWidth,
			buttonHeight,
			Component.translatable("nexeum.menu.button.accessibility"),
			Component.translatable("nexeum.menu.button.accessibility.detail"),
			0xFFF7C97B,
			button -> this.minecraft.setScreen(new AccessibilityOptionsScreen(this, this.minecraft.options))
		));
		addRenderableWidget(new NexeumMenuButton(
			rightX,
			startY + (buttonHeight + gap) * 3,
			columnWidth,
			buttonHeight,
			Component.translatable("nexeum.menu.button.quit"),
			Component.translatable("nexeum.menu.button.quit.detail"),
			0xFFFF8B8B,
			button -> this.minecraft.setScreen(new ConfirmScreen(confirmed -> {
				if (confirmed) {
					this.minecraft.stop();
				} else {
					this.minecraft.setScreen(this);
				}
			}, Component.translatable("nexeum.menu.quit.title"), Component.translatable("nexeum.menu.quit.detail.text")))
		));
	}
}
