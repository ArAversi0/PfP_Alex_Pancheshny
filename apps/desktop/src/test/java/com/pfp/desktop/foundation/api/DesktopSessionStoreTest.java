package com.pfp.desktop.foundation.api;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Path;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class DesktopSessionStoreTest {
    @TempDir
    Path tempDir;

    @Test
    void savesLoadsAndClearsAuthSession() throws Exception {
        DesktopSessionStore store = new DesktopSessionStore(tempDir);
        AuthSession session = new AuthSession(
                "access-token",
                "refresh-token",
                "Bearer",
                new AuthUser(UUID.randomUUID(), "hero@example.test", "USER", true)
        );

        store.save(session);

        assertThat(store.load()).contains(session);

        store.clear();

        assertThat(store.load()).isEmpty();
    }
}
