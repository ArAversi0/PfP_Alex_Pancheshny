package com.pfp.companion.content.entity;

import java.time.Instant;
import java.util.UUID;

public record LoreArticle(long id, String title, String content, String imageUrl, UUID updatedBy,
        Instant updatedAt) {

    public LoreArticle {
        requireText(title, "title");
        requireText(content, "content");
    }

    private static void requireText(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(field + " must not be blank");
        }
    }
}

