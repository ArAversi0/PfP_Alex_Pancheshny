package com.pfp.desktop.foundation.api;

import java.util.UUID;

public record AccountCharacterCard(
        UUID id,
        String name,
        int level,
        String className,
        String specialization,
        String imageUrl
) {
    public String classLine() {
        if (className != null && !className.isBlank()) {
            return className;
        }
        return "Unwritten class";
    }
}
