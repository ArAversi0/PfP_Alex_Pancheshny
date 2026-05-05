package com.pfp.gamerules;

public final class ProgressionCalculator {

    private static final int[] DIE_SIDES = {4, 6, 8, 10, 12};

    public RollRepresentation rollForLevel(int level) {
        requireNonNegative(level, "level");
        if (level == 0) {
            return RollRepresentation.constant(3);
        }
        int cycleIndex = (level - 1) % DIE_SIDES.length;
        int bonusSteps = (level - 1) / DIE_SIDES.length;
        return RollRepresentation.dice(3, DIE_SIDES[cycleIndex], bonusSteps * 13);
    }

    public RollRepresentation effectiveSkillRoll(int statLevel, int skillLevel) {
        requireNonNegative(statLevel, "statLevel");
        requireNonNegative(skillLevel, "skillLevel");
        return rollForLevel((statLevel + skillLevel) / 2);
    }

    public int passiveDodge(int dexterityLevel) {
        requireNonNegative(dexterityLevel, "dexterityLevel");
        if (dexterityLevel == 0) {
            return 3;
        }
        int cycleIndex = (dexterityLevel - 1) % DIE_SIDES.length;
        int completedCaps = (dexterityLevel - 1) / DIE_SIDES.length;
        return completedCaps * 12 + DIE_SIDES[cycleIndex];
    }

    private static void requireNonNegative(int value, String field) {
        if (value < 0) {
            throw new IllegalArgumentException(field + " must be non-negative");
        }
    }
}

