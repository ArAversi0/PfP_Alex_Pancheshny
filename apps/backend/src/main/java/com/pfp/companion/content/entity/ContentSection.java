package com.pfp.companion.content.entity;

public enum ContentSection {
    LORE,
    RULES;

    public static ContentSection fromApiPath(String value) {
        return switch (value) {
            case "lore" -> LORE;
            case "rules" -> RULES;
            default -> throw new IllegalArgumentException("unknown content section");
        };
    }
}
