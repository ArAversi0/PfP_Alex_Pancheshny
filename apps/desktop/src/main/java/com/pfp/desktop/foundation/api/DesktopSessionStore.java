package com.pfp.desktop.foundation.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

public final class DesktopSessionStore {
    private final Path sessionFile;
    private final ObjectMapper objectMapper;

    public DesktopSessionStore(Path storageRoot) {
        this(storageRoot.resolve("session.json"), new ObjectMapper());
    }

    DesktopSessionStore(Path sessionFile, ObjectMapper objectMapper) {
        this.sessionFile = sessionFile;
        this.objectMapper = objectMapper;
    }

    public Optional<AuthSession> load() throws IOException {
        if (!Files.exists(sessionFile)) {
            return Optional.empty();
        }
        return Optional.of(objectMapper.readValue(sessionFile.toFile(), AuthSession.class));
    }

    public void save(AuthSession session) throws IOException {
        Files.createDirectories(sessionFile.getParent());
        objectMapper.writerWithDefaultPrettyPrinter().writeValue(sessionFile.toFile(), session);
    }

    public void clear() throws IOException {
        Files.deleteIfExists(sessionFile);
    }
}
