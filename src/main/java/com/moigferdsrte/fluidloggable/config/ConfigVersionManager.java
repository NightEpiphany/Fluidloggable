package com.moigferdsrte.fluidloggable.config;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.moigferdsrte.fluidloggable.Fluidloggable;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Optional;

final class ConfigVersionManager {
	private static final String VERSION_KEY = "version";

	private ConfigVersionManager() {
	}

	static LoadResult prepare(final Path configPath) {
		return prepare(configPath, currentVersion());
	}

	static LoadResult prepare(final Path configPath, final String currentVersion) {
		if (!Files.exists(configPath)) {
			return new LoadResult(Optional.empty(), true);
		}

		final Optional<JsonObject> config = readConfig(configPath);
		final Optional<String> storedVersion = config.flatMap(ConfigVersionManager::readVersion);
		if (storedVersion.filter(currentVersion::equals).isPresent()) {
			return new LoadResult(config, true);
		}

		final String archiveVersion = storedVersion.orElse(config.isPresent() ? "legacy" : "invalid");
		if (archive(configPath, archiveVersion)) {
			return new LoadResult(Optional.empty(), true);
		}

		Fluidloggable.LOGGER.error(
				"Could not archive outdated Fluidloggable config {}; defaults will be used without overwriting it",
				configPath
		);
		return new LoadResult(Optional.empty(), false);
	}

	static String currentVersion() {
		return FabricLoader.getInstance()
				.getModContainer(Fluidloggable.MOD_ID)
				.map(container -> container.getMetadata().getVersion().getFriendlyString())
				.orElseGet(() -> {
					Fluidloggable.LOGGER.error("Fluidloggable mod metadata is unavailable; using unknown config version");
					return "unknown";
				});
	}

	private static Optional<JsonObject> readConfig(final Path configPath) {
		try (Reader reader = Files.newBufferedReader(configPath)) {
			final JsonElement root = JsonParser.parseReader(reader);
			return root.isJsonObject() ? Optional.of(root.getAsJsonObject()) : Optional.empty();
		} catch (RuntimeException | IOException exception) {
			Fluidloggable.LOGGER.warn("Failed to parse Fluidloggable config before version validation", exception);
			return Optional.empty();
		}
	}

	private static Optional<String> readVersion(final JsonObject config) {
		final JsonElement version = config.get(VERSION_KEY);
		if (version == null || !version.isJsonPrimitive() || !version.getAsJsonPrimitive().isString()) {
			return Optional.empty();
		}
		final String value = version.getAsString().trim();
		return value.isEmpty() ? Optional.empty() : Optional.of(value);
	}

	private static boolean archive(final Path configPath, final String storedVersion) {
		final Path archivePath = nextArchivePath(configPath, storedVersion);
		try {
			try {
				Files.move(configPath, archivePath, StandardCopyOption.ATOMIC_MOVE);
			} catch (AtomicMoveNotSupportedException exception) {
				Files.move(configPath, archivePath);
			}
			Fluidloggable.LOGGER.info("Archived outdated Fluidloggable config as {}", archivePath.getFileName());
			return true;
		} catch (IOException exception) {
			Fluidloggable.LOGGER.error("Failed to archive outdated Fluidloggable config to {}", archivePath, exception);
			return false;
		}
	}

	private static Path nextArchivePath(final Path configPath, final String storedVersion) {
		final String fileName = configPath.getFileName().toString();
		final String baseName = fileName.endsWith(".json")
				? fileName.substring(0, fileName.length() - ".json".length())
				: fileName;
		final String archiveBase = baseName + '-' + sanitizeVersion(storedVersion);
		Path candidate = configPath.resolveSibling(archiveBase + ".json.txt");
		int suffix = 1;
		while (Files.exists(candidate)) {
			candidate = configPath.resolveSibling(archiveBase + '-' + suffix++ + ".json.txt");
		}
		return candidate;
	}

	private static String sanitizeVersion(final String version) {
		final String sanitized = version.replaceAll("[^A-Za-z0-9._+-]", "_");
		return sanitized.isEmpty() ? "unknown" : sanitized;
	}

	record LoadResult(Optional<JsonObject> config, boolean shouldSave) {
	}
}
