package com.pfp.gamerules;

public record BodyPartHealth(int maxHp, int currentHp) {

    public BodyPartHealth {
        if (maxHp < 0) {
            throw new IllegalArgumentException("maxHp must be non-negative");
        }
        if (currentHp < 0 || currentHp > maxHp) {
            throw new IllegalArgumentException("currentHp must be between 0 and maxHp");
        }
    }

    public int damage() {
        return maxHp - currentHp;
    }
}

