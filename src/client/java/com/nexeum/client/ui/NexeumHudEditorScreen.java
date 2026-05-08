package com.nexeum.client.ui;

import com.nexeum.client.config.HudPosition;
import com.nexeum.client.config.NexeumConfig;
import com.nexeum.client.config.NexeumConfigManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public final class NexeumHudEditorScreen extends Screen {
	private final @Nullable Screen parent;
	private NexeumHudModule draggingModule;
	private NexeumHudModule selectedModule = NexeumHudModule.TARGET_HUD;
	private int dragOffsetX;
	private int dragOffsetY;

	public NexeumHudEditorScreen(@Nullable Screen parent) {
		super(Component.translatable("nexeum.editor.title"));
		this.parent = parent;
	}

	@Override
	protected void init() {
		int panelWidth = getControlPanelWidth();
		int panelX = getControlPanelX();
		int currentY = 18;

		for (NexeumHudModule module : NexeumHudRenderer.getMovableModules()) {
			final NexeumHudModule currentModule = module;
			this.addRenderableWidget(Button.builder(moduleLabel(module), button -> {
				this.selectedModule = currentModule;
				this.rebuildWidgets();
			}).bounds(panelX + 12, currentY, panelWidth - 24, 20).build());
			currentY += 22;
		}

		currentY += 6;

		this.addRenderableWidget(Button.builder(Component.translatable("nexeum.editor.nudge.up"), button -> nudgeSelected(0, -5))
			.bounds(panelX + 64, currentY, 48, 20)
			.build());
		currentY += 24;
		this.addRenderableWidget(Button.builder(Component.translatable("nexeum.editor.nudge.left"), button -> nudgeSelected(-5, 0))
			.bounds(panelX + 12, currentY, 48, 20)
			.build());
		this.addRenderableWidget(Button.builder(Component.translatable("nexeum.editor.nudge.right"), button -> nudgeSelected(5, 0))
			.bounds(panelX + 116, currentY, 48, 20)
			.build());
		currentY += 24;
		this.addRenderableWidget(Button.builder(Component.translatable("nexeum.editor.nudge.down"), button -> nudgeSelected(0, 5))
			.bounds(panelX + 64, currentY, 48, 20)
			.build());
		currentY += 30;

		if (NexeumHudRenderer.supportsLayoutToggle(this.selectedModule)) {
			this.addRenderableWidget(Button.builder(layoutLabel(), button -> {
				if (this.minecraft != null) {
					NexeumHudRenderer.toggleLayout(NexeumConfigManager.getConfig(), this.minecraft, this.selectedModule, this.width, this.height);
					NexeumConfigManager.save();
					this.rebuildWidgets();
				}
			}).bounds(panelX + 12, currentY, panelWidth - 24, 20).build());
			currentY += 24;
		}

		this.addRenderableWidget(Button.builder(Component.translatable("nexeum.editor.reset_module"), button -> {
			resetSelectedModule();
			NexeumConfigManager.save();
			this.rebuildWidgets();
		}).bounds(panelX + 12, currentY, panelWidth - 24, 20).build());
		currentY += 24;

		this.addRenderableWidget(Button.builder(Component.translatable("nexeum.editor.reset"), button -> {
			NexeumConfig config = NexeumConfigManager.getConfig();
			config.resetHudPositions();
			config.armorHudVertical = false;
			NexeumConfigManager.save();
			this.rebuildWidgets();
		}).bounds(panelX + 12, currentY, panelWidth - 24, 20).build());

		this.addRenderableWidget(Button.builder(Component.translatable("nexeum.button.close"), button -> this.onClose())
			.bounds(panelX + 12, this.height - 32, panelWidth - 24, 20)
			.build());
	}

	@Override
	public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
		if (this.minecraft != null && this.minecraft.level != null) {
			this.renderTransparentBackground(graphics);
			graphics.fill(0, 0, this.width, this.height, 0x4A07111F);
		} else {
			NexeumTheme.renderScreenBackground(graphics, this.width, this.height, System.currentTimeMillis());
		}

		renderEditorHeader(graphics);

		Minecraft client = Minecraft.getInstance();
		NexeumConfig config = NexeumConfigManager.getConfig();
		List<NexeumHudModule> modules = NexeumHudRenderer.getMovableModules();

		for (NexeumHudModule module : modules) {
			NexeumHudRenderer.renderEditorPreview(graphics, client, config, module);
		}

		for (NexeumHudModule module : modules) {
			NexeumHudRenderer.HudBounds bounds = NexeumHudRenderer.getModuleBounds(client, config, module, this.width, this.height, true);
			boolean hovered = isInside(mouseX, mouseY, bounds);
			int outlineColor = module == this.draggingModule
				? NexeumTheme.GOLD
				: module == this.selectedModule ? 0xFFFFB347 : hovered ? NexeumTheme.AQUA : 0xFF52779B;

			graphics.renderOutline(bounds.x() - 1, bounds.y() - 1, bounds.width() + 2, bounds.height() + 2, outlineColor);
			NexeumTheme.renderChip(
				graphics,
				this.font,
				bounds.x(),
				Math.max(6, bounds.y() - 16),
				Component.translatable(module.translationKey())
			);
		}

		renderControlPanel(graphics, client, config);
		super.render(graphics, mouseX, mouseY, partialTick);
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		if (super.mouseClicked(mouseX, mouseY, button)) {
			return true;
		}

		if (button != 0 || this.minecraft == null) {
			return false;
		}

		List<NexeumHudModule> modules = NexeumHudRenderer.getMovableModules();
		NexeumConfig config = NexeumConfigManager.getConfig();

		for (int index = modules.size() - 1; index >= 0; index--) {
			NexeumHudModule module = modules.get(index);
			NexeumHudRenderer.HudBounds bounds = NexeumHudRenderer.getModuleBounds(this.minecraft, config, module, this.width, this.height, true);

			if (isInside(mouseX, mouseY, bounds)) {
				this.selectedModule = module;
				this.draggingModule = module;
				this.dragOffsetX = (int) mouseX - bounds.x();
				this.dragOffsetY = (int) mouseY - bounds.y();
				return true;
			}
		}

		return false;
	}

	@Override
	public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
		if (this.draggingModule != null && button == 0 && this.minecraft != null) {
			NexeumHudRenderer.moveModule(
				NexeumConfigManager.getConfig(),
				this.minecraft,
				this.draggingModule,
				this.width,
				this.height,
				(int) mouseX - this.dragOffsetX,
				(int) mouseY - this.dragOffsetY
			);
			return true;
		}

		return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
	}

	@Override
	public boolean mouseReleased(double mouseX, double mouseY, int button) {
		if (this.draggingModule != null && button == 0) {
			this.draggingModule = null;
			NexeumConfigManager.save();
			this.rebuildWidgets();
			return true;
		}

		return super.mouseReleased(mouseX, mouseY, button);
	}

	@Override
	public void onClose() {
		NexeumConfigManager.save();
		if (this.minecraft != null) {
			this.minecraft.setScreen(this.parent);
		}
	}

	@Override
	public boolean isPauseScreen() {
		return false;
	}

	private void renderEditorHeader(GuiGraphics graphics) {
		int headerWidth = Math.max(80, Math.min(360, getControlPanelX() - 18));
		NexeumTheme.renderPanel(graphics, 12, 12, headerWidth, 62);
		graphics.drawString(this.font, this.title, 24, 24, NexeumTheme.PANEL_TEXT);
		graphics.drawWordWrap(this.font, Component.translatable("nexeum.editor.desc"), 24, 38, Math.max(50, headerWidth - 30), NexeumTheme.SUBTLE_TEXT);
	}

	private void renderControlPanel(GuiGraphics graphics, Minecraft client, NexeumConfig config) {
		int panelWidth = getControlPanelWidth();
		int panelX = getControlPanelX();
		NexeumTheme.renderPanel(graphics, panelX, 12, panelWidth, this.height - 24);

		graphics.drawString(this.font, Component.translatable("nexeum.editor.selected"), panelX + 14, 138, NexeumTheme.GOLD);
		graphics.drawString(this.font, Component.translatable(this.selectedModule.translationKey()), panelX + 14, 151, NexeumTheme.PANEL_TEXT);

		NexeumHudRenderer.HudBounds bounds = NexeumHudRenderer.getModuleBounds(client, config, this.selectedModule, this.width, this.height, true);
		graphics.drawString(this.font, Component.translatable("nexeum.editor.coords", Integer.toString(bounds.x()), Integer.toString(bounds.y())), panelX + 14, 168, NexeumTheme.SUBTLE_TEXT);
		graphics.drawWordWrap(this.font, Component.translatable("nexeum.editor.drag_hint"), panelX + 14, 184, panelWidth - 28, NexeumTheme.SUBTLE_TEXT);

		if (NexeumHudRenderer.supportsLayoutToggle(this.selectedModule)) {
			String stateKey = NexeumHudRenderer.isVerticalLayout(config, this.selectedModule) ? "nexeum.editor.layout.vertical" : "nexeum.editor.layout.horizontal";
			graphics.drawString(this.font, Component.translatable("nexeum.editor.layout", Component.translatable(stateKey)), panelX + 14, 228, NexeumTheme.SUBTLE_TEXT);
		}
	}

	private void nudgeSelected(int deltaX, int deltaY) {
		if (this.minecraft == null) {
			return;
		}

		NexeumHudRenderer.nudgeModule(NexeumConfigManager.getConfig(), this.minecraft, this.selectedModule, this.width, this.height, deltaX, deltaY);
		NexeumConfigManager.save();
	}

	private void resetSelectedModule() {
		NexeumConfig config = NexeumConfigManager.getConfig();
		switch (this.selectedModule) {
			case TARGET_HUD -> config.targetHudPosition = new HudPosition(0.5D, 0.06D);
			case COOLDOWN_METER -> config.cooldownMeterPosition = new HudPosition(0.5D, 0.56D);
			case MACE_DAMAGE -> config.maceDamageHudPosition = new HudPosition(0.72D, 0.58D);
			case INFO_PANEL -> config.infoPanelPosition = new HudPosition(0.02D, 0.03D);
			case KEYSTROKES -> config.keystrokesPosition = new HudPosition(0.02D, 0.82D);
			case ARMOR_HUD -> {
				config.armorHudPosition = new HudPosition(0.5D, 0.88D);
				config.armorHudVertical = false;
			}
		}
	}

	private Component moduleLabel(NexeumHudModule module) {
		String wrapper = module == this.selectedModule ? "nexeum.tab.active" : "nexeum.tab.inactive";
		return Component.translatable(wrapper, Component.translatable(module.translationKey()));
	}

	private Component layoutLabel() {
		String stateKey = NexeumHudRenderer.isVerticalLayout(NexeumConfigManager.getConfig(), this.selectedModule)
			? "nexeum.editor.layout.vertical"
			: "nexeum.editor.layout.horizontal";
		return Component.translatable("nexeum.editor.toggle_layout", Component.translatable(stateKey));
	}

	private static boolean isInside(double mouseX, double mouseY, NexeumHudRenderer.HudBounds bounds) {
		return mouseX >= bounds.x() && mouseX <= bounds.x() + bounds.width()
			&& mouseY >= bounds.y() && mouseY <= bounds.y() + bounds.height();
	}

	private int getControlPanelWidth() {
		return Math.min(188, Math.max(156, this.width / 3));
	}

	private int getControlPanelX() {
		return this.width - getControlPanelWidth() - 12;
	}
}
