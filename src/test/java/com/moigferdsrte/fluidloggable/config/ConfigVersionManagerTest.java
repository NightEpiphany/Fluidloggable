package com.moigferdsrte.fluidloggable.config;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ConfigVersionManagerTest {
	@TempDir
	Path tempDirectory;

	@Test
	void keepsMatchingConfigActive() throws IOException {
		final Path config = writeConfig("3.1.2-beta.2-mc26.2", "custom");

		final ConfigVersionManager.LoadResult result = ConfigVersionManager.prepare(
				config,
				"3.1.2-beta.2-mc26.2"
		);

		assertTrue(result.config().isPresent());
		assertTrue(result.shouldSave());
		assertTrue(Files.exists(config));
		assertEquals("custom", result.config().orElseThrow().get("marker").getAsString());
	}

	@Test
	void archivesMismatchedConfigWithStoredVersion() throws IOException {
		final Path config = writeConfig("3.1.2-beta.1-mc26.2", "preserved");

		final ConfigVersionManager.LoadResult result = ConfigVersionManager.prepare(
				config,
				"3.1.2-beta.2-mc26.2"
		);

		final Path archive = tempDirectory.resolve("fluidloggable-3.1.2-beta.1-mc26.2.json.txt");
		assertTrue(result.config().isEmpty());
		assertTrue(result.shouldSave());
		assertFalse(Files.exists(config));
		assertTrue(Files.readString(archive).contains("preserved"));
	}

	@Test
	void archivesVersionlessConfigAsLegacy() throws IOException {
		final Path config = tempDirectory.resolve("fluidloggable.json");
		Files.writeString(config, "{\"marker\":\"legacy\"}");

		ConfigVersionManager.prepare(config, "3.1.2-beta.2-mc26.2");

		assertTrue(Files.exists(tempDirectory.resolve("fluidloggable-legacy.json.txt")));
	}

	@Test
	void preservesExistingArchiveByAddingSuffix() throws IOException {
		final Path config = writeConfig("3.1.2-beta.1-mc26.2", "new archive");
		final Path existingArchive = tempDirectory.resolve("fluidloggable-3.1.2-beta.1-mc26.2.json.txt");
		Files.writeString(existingArchive, "existing archive");

		ConfigVersionManager.prepare(config, "3.1.2-beta.2-mc26.2");

		assertEquals("existing archive", Files.readString(existingArchive));
		assertTrue(Files.readString(
				tempDirectory.resolve("fluidloggable-3.1.2-beta.1-mc26.2-1.json.txt")
		).contains("new archive"));
	}

	private Path writeConfig(final String version, final String marker) throws IOException {
		final Path config = tempDirectory.resolve("fluidloggable.json");
		Files.writeString(config, "{\"version\":\"" + version + "\",\"marker\":\"" + marker + "\"}");
		return config;
	}
}
