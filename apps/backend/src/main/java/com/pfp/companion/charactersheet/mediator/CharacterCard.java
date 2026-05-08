package com.pfp.companion.charactersheet.mediator;

import java.time.Instant;
import java.util.UUID;

public record CharacterCard(UUID id, String name, String image, int level, String className,
        String specialization, Instant createdAt) {
}
