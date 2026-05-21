package com.pfp.companion.content.entity;

public record RuleBookArticle(long id, long categoryId, String title, String content) {

    public RuleBookArticle {
        if (categoryId <= 0) {
            throw new IllegalArgumentException("categoryId must be positive");
        }
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("title must not be blank");
        }
        if (content == null || content.isBlank()) {
            throw new IllegalArgumentException("content must not be blank");
        }
    }
}

