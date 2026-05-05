package com.pfp.gamerules;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class ProgressionCalculatorTest {

    private final ProgressionCalculator calculator = new ProgressionCalculator();

    @Test
    void mapsStatProgressionAcrossCaps() {
        assertEquals("3", calculator.rollForLevel(0).display());
        assertEquals("3d4", calculator.rollForLevel(1).display());
        assertEquals("3d12", calculator.rollForLevel(5).display());
        assertEquals("3d4+13", calculator.rollForLevel(6).display());
        assertEquals("3d4+26", calculator.rollForLevel(11).display());
    }

    @Test
    void calculatesEffectiveSkillRollFromAverageRoundedDown() {
        assertEquals("3d8", calculator.effectiveSkillRoll(4, 2).display());
    }

    @Test
    void calculatesPassiveDodgeAcrossCaps() {
        assertEquals(3, calculator.passiveDodge(0));
        assertEquals(10, calculator.passiveDodge(4));
        assertEquals(18, calculator.passiveDodge(7));
        assertEquals(28, calculator.passiveDodge(11));
    }

    @Test
    void rejectsNegativeLevels() {
        assertThrows(IllegalArgumentException.class, () -> calculator.rollForLevel(-1));
    }
}

