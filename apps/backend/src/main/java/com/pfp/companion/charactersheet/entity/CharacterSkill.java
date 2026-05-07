package com.pfp.companion.charactersheet.entity;

import java.util.Objects;

public record CharacterSkill(SkillName name, int level) {

    public CharacterSkill {
        Objects.requireNonNull(name, "name must not be null");
        if (level < 0) {
            throw new IllegalArgumentException("skill level must be non-negative");
        }
    }

    public StatGroup statGroup() {
        return name.statGroup();
    }
}

