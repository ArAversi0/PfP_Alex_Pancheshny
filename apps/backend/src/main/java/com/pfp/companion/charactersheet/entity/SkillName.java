package com.pfp.companion.charactersheet.entity;

public enum SkillName {
    ATHLETICS(StatGroup.STRENGTH),
    BLOCKING(StatGroup.STRENGTH),
    GRAPPLING(StatGroup.STRENGTH),
    BRUTE_FORCE(StatGroup.STRENGTH),
    REFLEXES(StatGroup.DEXTERITY),
    EVASION(StatGroup.DEXTERITY),
    ACROBATICS(StatGroup.DEXTERITY),
    STEALTH(StatGroup.DEXTERITY),
    SLEIGHT_OF_HAND(StatGroup.DEXTERITY),
    BALANCE(StatGroup.DEXTERITY),
    PAIN_TOLERANCE(StatGroup.STAMINA),
    ENDURANCE(StatGroup.STAMINA),
    CARRYING_CAPACITY(StatGroup.STAMINA),
    ANALYSIS(StatGroup.INTELLIGENCE),
    MAGIC(StatGroup.INTELLIGENCE),
    RELIGION(StatGroup.INTELLIGENCE),
    SURVIVAL(StatGroup.INTELLIGENCE),
    MEDICINE(StatGroup.INTELLIGENCE),
    SCIENCE(StatGroup.INTELLIGENCE),
    AWARENESS(StatGroup.INTELLIGENCE),
    RHETORIC(StatGroup.CHARISMA),
    PERFORMANCE(StatGroup.CHARISMA),
    INTIMIDATION(StatGroup.CHARISMA),
    CHARM(StatGroup.CHARISMA),
    BUSINESS_SENSE(StatGroup.CHARISMA),
    FORTUNE(StatGroup.LUCK),
    PROFIT(StatGroup.LUCK),
    WILLPOWER(StatGroup.MIND),
    COMPOSURE(StatGroup.MIND),
    SUGGESTION(StatGroup.MIND);

    private final StatGroup statGroup;

    SkillName(StatGroup statGroup) {
        this.statGroup = statGroup;
    }

    public StatGroup statGroup() {
        return statGroup;
    }
}

