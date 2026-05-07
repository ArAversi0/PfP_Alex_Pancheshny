package com.pfp.companion.charactersheet.entity;

public record BlessingInspiration(int blessings, int inspirations) {

    public BlessingInspiration {
        if (blessings < 0 || inspirations < 0) {
            throw new IllegalArgumentException("blessings and inspirations must be non-negative");
        }
    }

    public static BlessingInspiration empty() {
        return new BlessingInspiration(0, 0);
    }
}

