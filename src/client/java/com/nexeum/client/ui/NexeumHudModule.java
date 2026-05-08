package com.nexeum.client.ui;

public enum NexeumHudModule {
	TARGET_HUD("nexeum.editor.module.target"),
	COOLDOWN_METER("nexeum.editor.module.cooldown"),
	MACE_DAMAGE("nexeum.editor.module.mace"),
	INFO_PANEL("nexeum.editor.module.info"),
	KEYSTROKES("nexeum.editor.module.keystrokes"),
	ARMOR_HUD("nexeum.editor.module.armor");

	private final String translationKey;

	NexeumHudModule(String translationKey) {
		this.translationKey = translationKey;
	}

	public String translationKey() {
		return this.translationKey;
	}
}
