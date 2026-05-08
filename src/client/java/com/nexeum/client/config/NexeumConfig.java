package com.nexeum.client.config;

public final class NexeumConfig {
	public boolean autoSprintEnabled = true;
	public boolean targetHudEnabled = true;
	public boolean cooldownMeterEnabled = true;
	public boolean armorHudEnabled = true;
	public boolean maceDamageHudEnabled = true;
	public boolean keystrokesHudEnabled = true;
	public boolean cpsHudEnabled = true;
	public boolean infoHudEnabled = true;
	public boolean zoomEnabled = true;
	public boolean fullbrightEnabled = false;
	public boolean starCrosshairEnabled = true;
	public boolean nebulaOverlayEnabled = true;
	public boolean starHaloEnabled = true;
	public boolean cometTrailEnabled = true;
	public boolean starMenuEnabled = true;
	public boolean armorHudVertical = false;

	public HudPosition targetHudPosition = new HudPosition(0.5D, 0.06D);
	public HudPosition cooldownMeterPosition = new HudPosition(0.5D, 0.56D);
	public HudPosition infoPanelPosition = new HudPosition(0.02D, 0.03D);
	public HudPosition keystrokesPosition = new HudPosition(0.02D, 0.82D);
	public HudPosition armorHudPosition = new HudPosition(0.5D, 0.88D);
	public HudPosition maceDamageHudPosition = new HudPosition(0.72D, 0.58D);

	public void ensureDefaults() {
		if (this.targetHudPosition == null) {
			this.targetHudPosition = new HudPosition(0.5D, 0.06D);
		}

		if (this.cooldownMeterPosition == null) {
			this.cooldownMeterPosition = new HudPosition(0.5D, 0.56D);
		}

		if (this.infoPanelPosition == null) {
			this.infoPanelPosition = new HudPosition(0.02D, 0.03D);
		}

		if (this.keystrokesPosition == null) {
			this.keystrokesPosition = new HudPosition(0.02D, 0.82D);
		}

		if (this.armorHudPosition == null) {
			this.armorHudPosition = new HudPosition(0.5D, 0.88D);
		}

		if (this.maceDamageHudPosition == null) {
			this.maceDamageHudPosition = new HudPosition(0.72D, 0.58D);
		}
	}

	public void resetHudPositions() {
		this.targetHudPosition = new HudPosition(0.5D, 0.06D);
		this.cooldownMeterPosition = new HudPosition(0.5D, 0.56D);
		this.infoPanelPosition = new HudPosition(0.02D, 0.03D);
		this.keystrokesPosition = new HudPosition(0.02D, 0.82D);
		this.armorHudPosition = new HudPosition(0.5D, 0.88D);
		this.maceDamageHudPosition = new HudPosition(0.72D, 0.58D);
	}
}
