package com.pfp.companion.charactersheet.entity;

import java.util.Objects;
import java.util.UUID;

public record Spell(UUID id, String name, SpellType type, SpellClass spellClass, String image,
        String requirements, String description) {

    public Spell {
        Objects.requireNonNull(id);
        requireText(name, "name");
        Objects.requireNonNull(type);
        Objects.requireNonNull(spellClass);
        Objects.requireNonNull(image);
        requireText(requirements, "requirements");
        description = description == null ? "" : description;
    }

    private static void requireText(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(field + " must not be blank");
        }
    }
}
