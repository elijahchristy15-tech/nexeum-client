package com.nexeum.client.ui;

import com.nexeum.client.config.NexeumConfig;
import com.nexeum.client.config.NexeumConfigManager;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

public final class NexeumConfigScreen extends Screen {
	private final @Nullable Screen parent;
	private Tab activeTab;

	public NexeumConfigScreen(@Nullable Screen parent) {
		this(parent, Tab.MODULES);
	}

	public NexeumConfigScreen(@Nullable Screen parent, Tab activeTab) {
		super(Component.translatable("nexeum.title"));
		this.parent = parent;
		this.activeTab = activeTab;
	}

	@Override
	protected void init() {
		NexeumConfig config = NexeumConfigManager.getConfig();

		int panelWidth = Math.min(404, this.width - 12);
		int panelHeight = Math.min(244, this.height - 12);
		int panelX = (this.width - panelWidth) / 2;
		int panelY = Math.max(6, (this.height - panelHeight) / 2);

		int leftX = panelX + 20;
		int rightX = panelX + 212;
		int columnWidth = 172;
		int startY = panelY + 90;
		int rowGap = 20;
		int toggleHeight = 18;

		this.addRenderableWidget(Button.builder(tabLabel(Tab.MODULES), button -> switchTab(Tab.MODULES))
			.bounds(panelX + 20, panelY + 48, 172, 20)
			.build());
		this.addRenderableWidget(Button.builder(tabLabel(Tab.COSMETICS), button -> switchTab(Tab.COSMETICS))
			.bounds(panelX + 212, panelY + 48, 172, 20)
			.build());

		if (this.activeTab == Tab.MODULES) {
			this.addRenderableWidget(Button.builder(Component.translatable("nexeum.button.edit_hud"), button -> {
				if (this.minecraft != null) {
					this.minecraft.setScreen(new NexeumHudEditorScreen(this));
				}
			}).bounds(panelX + panelWidth - 116, panelY + 72, 96, 18).build());

			addToggleButton(leftX, startY, columnWidth, toggleHeight, optionLabel("nexeum.option.auto_sprint", config.autoSprintEnabled), () -> {
				config.autoSprintEnabled = !config.autoSprintEnabled;
			});
			addToggleButton(leftX, startY + rowGap, columnWidth, toggleHeight, optionLabel("nexeum.option.target_hud", config.targetHudEnabled), () -> {
				config.targetHudEnabled = !config.targetHudEnabled;
			});
			addToggleButton(leftX, startY + rowGap * 2, columnWidth, toggleHeight, optionLabel("nexeum.option.cooldown_meter", config.cooldownMeterEnabled), () -> {
				config.cooldownMeterEnabled = !config.cooldownMeterEnabled;
			});
			addToggleButton(leftX, startY + rowGap * 3, columnWidth, toggleHeight, optionLabel("nexeum.option.armor_hud", config.armorHudEnabled), () -> {
				config.armorHudEnabled = !config.armorHudEnabled;
			});
			addToggleButton(leftX, startY + rowGap * 4, columnWidth, toggleHeight, optionLabel("nexeum.option.keystrokes_hud", config.keystrokesHudEnabled), () -> {
				config.keystrokesHudEnabled = !config.keystrokesHudEnabled;
			});
			addToggleButton(rightX, startY, columnWidth, toggleHeight, optionLabel("nexeum.option.cps_hud", config.cpsHudEnabled), () -> {
				config.cpsHudEnabled = !config.cpsHudEnabled;
			});
			addToggleButton(rightX, startY + rowGap, columnWidth, toggleHeight, optionLabel("nexeum.option.info_hud", config.infoHudEnabled), () -> {
				config.infoHudEnabled = !config.infoHudEnabled;
			});
			addToggleButton(rightX, startY + rowGap * 2, columnWidth, toggleHeight, optionLabel("nexeum.option.zoom", config.zoomEnabled), () -> {
				config.zoomEnabled = !config.zoomEnabled;
			});
			addToggleButton(rightX, startY + rowGap * 3, columnWidth, toggleHeight, optionLabel("nexeum.option.fullbright", config.fullbrightEnabled), () -> {
				config.fullbrightEnabled = !config.fullbrightEnabled;
			});
			addToggleButton(rightX, startY + rowGap * 4, columnWidth, toggleHeight, optionLabel("nexeum.option.mace_damage_hud", config.maceDamageHudEnabled), () -> {
				config.maceDamageHudEnabled = !config.maceDamageHudEnabled;
			});
		} else {
			addToggleButton(leftX, startY, columnWidth, toggleHeight, optionLabel("nexeum.option.star_crosshair", config.starCrosshairEnabled), () -> {
				config.starCrosshairEnabled = !config.starCrosshairEnabled;
			});
			addToggleButton(leftX, startY + rowGap, columnWidth, toggleHeight, optionLabel("nexeum.option.nebula_overlay", config.nebulaOverlayEnabled), () -> {
				config.nebulaOverlayEnabled = !config.nebulaOverlayEnabled;
			});
			addToggleButton(leftX, startY + rowGap * 2, columnWidth, toggleHeight, optionLabel("nexeum.option.star_halo", config.starHaloEnabled), () -> {
				config.starHaloEnabled = !config.starHaloEnabled;
			});
			addToggleButton(leftX, startY + rowGap * 3, columnWidth, toggleHeight, optionLabel("nexeum.option.comet_trail", config.cometTrailEnabled), () -> {
				config.cometTrailEnabled = !config.cometTrailEnabled;
			});
			addToggleButton(rightX, startY, columnWidth, toggleHeight, optionLabel("nexeum.option.star_menu", config.starMenuEnabled), () -> {
				config.starMenuEnabled = !config.starMenuEnabled;
			});
		}

		int bottomY = panelY + panelHeight - 28;

		this.addRenderableWidget(Button.builder(Component.translatable("nexeum.button.reset"), button -> {
			NexeumConfigManager.reset();
			this.rebuildWidgets();
		}).bounds(panelX + 20, bottomY, 126, 20).build());

		this.addRenderableWidget(Button.builder(Component.translatable("nexeum.button.close"), button -> this.onClose())
			.bounds(panelX + panelWidth - 146, bottomY, 126, 20)
			.build());
	}

	@Override
	public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
		NexeumTheme.renderScreenBackground(graphics, this.width, this.height, System.currentTimeMillis());

		int panelWidth = Math.min(404, this.width - 12);
		int panelHeight = Math.min(244, this.height - 12);
		int panelX = (this.width - panelWidth) / 2;
		int panelY = Math.max(6, (this.height - panelHeight) / 2);

		NexeumTheme.renderPanel(graphics, panelX, panelY, panelWidth, panelHeight);

		graphics.drawCenteredString(this.font, this.title, this.width / 2, panelY + 12, NexeumTheme.PANEL_TEXT);
		graphics.drawCenteredString(this.font, Component.translatable("nexeum.subtitle"), this.width / 2, panelY + 26, NexeumTheme.SUBTLE_TEXT);

		graphics.drawString(this.font, Component.translatable(this.activeTab == Tab.MODULES ? "nexeum.section.modules" : "nexeum.section.cosmetics"), panelX + 20, panelY + 76, NexeumTheme.GOLD);
		graphics.drawString(this.font, Component.translatable("nexeum.section.controls.desc"), panelX + 20, panelY + panelHeight - 42, NexeumTheme.SUBTLE_TEXT);

		super.render(graphics, mouseX, mouseY, partialTick);
	}

	@Override
	public void onClose() {
		if (this.minecraft != null) {
			this.minecraft.setScreen(this.parent);
		}
	}

	private void addToggleButton(int x, int y, int width, int height, Component message, Runnable toggleAction) {
		this.addRenderableWidget(Button.builder(message, button -> {
			toggleAction.run();
			NexeumConfigManager.save();
			this.rebuildWidgets();
		}).bounds(x, y, width, height).build());
	}

	private void switchTab(Tab tab) {
		this.activeTab = tab;
		this.rebuildWidgets();
	}

	private Component tabLabel(Tab tab) {
		String key = tab == this.activeTab ? "nexeum.tab.active" : "nexeum.tab.inactive";
		return Component.translatable(key, Component.translatable(tab.translationKey));
	}

	private static Component optionLabel(String key, boolean enabled) {
		return Component.translatable(key, NexeumTheme.toggleState(enabled));
	}

	public enum Tab {
		MODULES("nexeum.tab.modules"),
		COSMETICS("nexeum.tab.cosmetics");

		private final String translationKey;

		Tab(String translationKey) {
			this.translationKey = translationKey;
		}
	}
}
