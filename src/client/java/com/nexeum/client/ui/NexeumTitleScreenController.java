package com.nexeum.client.ui;

import com.nexeum.client.config.NexeumConfigManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.TitleScreen;

public final class NexeumTitleScreenController {
	private NexeumTitleScreenController() {
	}

	public static void sync(Minecraft client) {
		if (client.screen == null) {
			return;
		}

		boolean starMenuEnabled = NexeumConfigManager.getConfig().starMenuEnabled;

		if (starMenuEnabled && client.screen.getClass() == TitleScreen.class) {
			client.setScreen(new NexeumTitleScreen());
			return;
		}

		if (!starMenuEnabled && client.screen instanceof NexeumTitleScreen) {
			client.setScreen(new TitleScreen());
		}
	}
}
