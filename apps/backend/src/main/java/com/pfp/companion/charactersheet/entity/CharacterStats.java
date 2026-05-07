package com.pfp.companion.charactersheet.entity;

import java.util.EnumMap;
import java.util.Map;

public final class CharacterStats {

    private final EnumMap<StatGroup, Integer> levels;

    public CharacterStats(Map<StatGroup, Integer> levels) {
        this.levels = new EnumMap<>(StatGroup.class);
        for (StatGroup statGroup : StatGroup.values()) {
            Integer level = levels.get(statGroup);
            if (level == null || level < 0) {
                throw new IllegalArgumentException("all stat levels must be non-negative");
            }
            this.levels.put(statGroup, level);
        }
    }

    public static CharacterStats zeroed() {
        EnumMap<StatGroup, Integer> levels = new EnumMap<>(StatGroup.class);
        for (StatGroup statGroup : StatGroup.values()) {
            levels.put(statGroup, 0);
        }
        return new CharacterStats(levels);
    }

    public int level(StatGroup statGroup) {
        return levels.get(statGroup);
    }

    public Map<StatGroup, Integer> levels() {
        return Map.copyOf(levels);
    }
}

