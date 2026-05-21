package com.pfp.companion.content.entity;

import java.time.Instant;

public record ContentNode(long id, ContentSection section, String slug, String parentSlug,
        String title, String summary, String contentMarkdown, int sortOrder, boolean published,
        Instant updatedAt) {

    public ContentNode {
        if (section == null) {
            throw new IllegalArgumentException("section must not be null");
        }
        requireText(slug, "slug");
        requireText(title, "title");
        summary = summary == null ? "" : summary;
        contentMarkdown = contentMarkdown == null ? "" : contentMarkdown;
        if (parentSlug != null && parentSlug.isBlank()) {
            parentSlug = null;
        }
    }

    public boolean hasContent() {
        return !contentMarkdown.isBlank();
    }

    private static void requireText(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(field + " must not be blank");
        }
    }
}
