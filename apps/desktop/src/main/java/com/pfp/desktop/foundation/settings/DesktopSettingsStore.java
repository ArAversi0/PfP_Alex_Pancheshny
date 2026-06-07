package com.pfp.desktop.foundation.settings;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class DesktopSettingsStore {
    private final Path settingsFile;
    private final ObjectMapper objectMapper;

    public DesktopSettingsStore(Path storageRoot) {
        this(storageRoot.resolve("settings.json"), new ObjectMapper());
    }

    DesktopSettingsStore(Path settingsFile, ObjectMapper objectMapper) {
        this.settingsFile = settingsFile;
        this.objectMapper = objectMapper;
    }

    public DesktopSettings load() throws IOException {
        if (!Files.exists(settingsFile)) {
            return DesktopSettings.defaults();
        }
        return objectMapper.readValue(settingsFile.toFile(), DesktopSettings.class);
    }

    public void save(DesktopSettings settings) throws IOException {
        Files.createDirectories(settingsFile.getParent());
        objectMapper.writerWithDefaultPrettyPrinter().writeValue(settingsFile.toFile(), settings);
    }
}
