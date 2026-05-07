package com.pfp.companion.charactersheet.entity;

public record CharacterInfo(int level, String origin, String background, String className,
        String specialization) {

    public CharacterInfo {
        if (level <= 0) {
            throw new IllegalArgumentException("level must be positive");
        }
        origin = normalized(origin);
        background = normalized(background);
        className = normalized(className);
        specialization = normalized(specialization);
    }

    public static CharacterInfo empty() {
        return new CharacterInfo(1, "", "", "", "");
    }

    private static String normalized(String value) {
        return value == null ? "" : value;
    }
}
