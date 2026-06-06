package com.pfp.desktop.foundation.content;

public enum ContentSection {
    LORE("lore", "Lore", "World archive", "Browse the known histories and places of the PfP world."),
    RULES("rules", "Rule book", "Reference archive", "Browse categories and articles from the PfP rules archive.");

    private final String id;
    private final String title;
    private final String eyebrow;
    private final String description;

    ContentSection(String id, String title, String eyebrow, String description) {
        this.id = id;
        this.title = title;
        this.eyebrow = eyebrow;
        this.description = description;
    }

    public String id() {
        return id;
    }

    public String title() {
        return title;
    }

    public String eyebrow() {
        return eyebrow;
    }

    public String description() {
        return description;
    }

    public static ContentSection fromId(String id) {
        if ("rules".equalsIgnoreCase(id)) {
            return RULES;
        }
        return LORE;
    }
}
