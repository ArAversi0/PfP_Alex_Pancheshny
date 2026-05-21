package com.pfp.companion.content.entity;

public record RuleCategory(long id, String name, String description) {

    public RuleCategory {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("name must not be blank");
        }
        description = description == null ? "" : description;
    }
}

