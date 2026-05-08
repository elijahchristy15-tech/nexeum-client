package com.nexeum.client;

import com.mojang.blaze3d.platform.InputConstants;
import com.nexeum.client.config.NexeumConfigManager;
import com.nexeum.client.runtime.NexeumRuntime;
import com.nexeum.client.ui.NexeumConfigScreen;
import com.nexeum.client.ui.NexeumHudRenderer;
import com.nexeum.client.ui.NexeumTitleScreenController;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import org.lwjgl.glfw.GLFW;

public final class NexeumClient implements ClientModInitializer {
	private static KeyMapping openConfigKey;
	private static KeyMapping zoomKey;

	@Override
	public void onInitializeClient() {
		NexeumConfigManager.load();

		openConfigKey = KeyBindingHelper.registerKeyBinding(new KeyMapping(
			"key.nexeum.open_config",
			InputConstants.Type.KEYSYM,
			GLFW.GLFW_KEY_RIGHT_SHIFT,
			"category.nexeum"
		));

		zoomKey = KeyBindingHelper.registerKeyBinding(new KeyMapping(
			"key.nexeum.zoom",
			InputConstants.Type.KEYSYM,
			GLFW.GLFW_KEY_C,
			"category.nexeum"
		));

		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			NexeumTitleScreenController.sync(client);

			while (openConfigKey.consumeClick()) {
				openConfig(client);
			}

			NexeumRuntime.tick(client, zoomKey.isDown());
		});

		ClientLifecycleEvents.CLIENT_STOPPING.register(NexeumRuntime::restoreTransientOptions);
		HudRenderCallback.EVENT.register(NexeumHudRenderer::render);
	}

	private static void openConfig(Minecraft client) {
		client.setScreen(new NexeumConfigScreen(client.screen));
	}
}
