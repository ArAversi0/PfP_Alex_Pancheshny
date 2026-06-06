package com.pfp.desktop.foundation.content;

import java.util.List;

public record ContentNode(
        String slug,
        String parentSlug,
        String title,
        String summary,
        String contentMarkdown,
        int sortOrder,
        List<ContentNode> children
) {
    public ContentNode {
        slug = text(slug);
        parentSlug = parentSlug == null || parentSlug.isBlank() ? null : parentSlug;
        title = text(title);
        summary = text(summary);
        contentMarkdown = text(contentMarkdown);
        children = children == null ? List.of() : List.copyOf(children);
    }

    public boolean category() {
        return !children.isEmpty();
    }

    private static String text(String value) {
        return value == null ? "" : value;
    }
}
