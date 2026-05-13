package com.pfp.companion.charactersheet.control;

import java.util.UUID;

public record CharacterCardResponse(UUID id, String name, int level, String className,
        String specialization, String imageUrl) {
}
