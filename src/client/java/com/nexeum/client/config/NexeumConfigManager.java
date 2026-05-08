package com.nexeum.client.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.nexeum.NexeumMod;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public final class NexeumConfigManager {
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	private static final Path CONFIG_PATH = FabricLoader.getInstance().getConfigDir().resolve("nexeum-client.json");

	private static NexeumConfig config = new NexeumConfig();

	private NexeumConfigManager() {
	}

	public static NexeumConfig getConfig() {
		return config;
	}

	public static void load() {
		if (!Files.exists(CONFIG_PATH)) {
			save();
			return;
		}

		try {
			String json = Files.readString(CONFIG_PATH, StandardCharsets.UTF_8);
			NexeumConfig loaded = GSON.fromJson(json, NexeumConfig.class);
			config = loaded != null ? loaded : new NexeumConfig();
			config.ensureDefaults();
		} catch (IOException exception) {
			NexeumMod.LOGGER.error("Failed to load Nexeum config from {}", CONFIG_PATH, exception);
			config = new NexeumConfig();
		}
	}

	public static void save() {
		try {
			Files.createDirectories(CONFIG_PATH.getParent());
			Files.writeString(CONFIG_PATH, GSON.toJson(config), StandardCharsets.UTF_8);
		} catch (IOException exception) {
			NexeumMod.LOGGER.error("Failed to save Nexeum config to {}", CONFIG_PATH, exception);
		}
	}

	public static void reset() {
		config = new NexeumConfig();
		save();
	}
}
