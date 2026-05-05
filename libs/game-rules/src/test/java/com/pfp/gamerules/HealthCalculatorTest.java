package com.pfp.gamerules;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.EnumMap;
import java.util.Map;
import org.junit.jupiter.api.Test;

class HealthCalculatorTest {

    private final HealthCalculator calculator = new HealthCalculator();

    @Test
    void calculatesWeightedGlobalHealthFromDamage() {
        Map<BodyPart, BodyPartHealth> health = healthyBody();
        health.put(BodyPart.HEAD, new BodyPartHealth(60, 30));
        health.put(BodyPart.TORSO, new BodyPartHealth(100, 85));
        health.put(BodyPart.LEFT_ARM, new BodyPartHealth(60, 20));

        HealthResult result = calculator.calculate(health);

        assertEquals(new BigDecimal("12.78"), result.globalHealthPercent());
        assertFalse(result.dead());
    }

    @Test
    void detectsCriticalZoneDeath() {
        Map<BodyPart, BodyPartHealth> health = healthyBody();
        health.put(BodyPart.NECK, new BodyPartHealth(40, 0));

        assertTrue(calculator.calculate(health).dead());
    }

    @Test
    void detectsThreeDestroyedLimbsDeath() {
        Map<BodyPart, BodyPartHealth> health = healthyBody();
        health.put(BodyPart.LEFT_ARM, new BodyPartHealth(60, 0));
        health.put(BodyPart.RIGHT_ARM, new BodyPartHealth(60, 0));
        health.put(BodyPart.LEFT_LEG, new BodyPartHealth(60, 0));

        assertTrue(calculator.calculate(health).dead());
    }

    @Test
    void usesEditedMaxHpAsDynamicZoneDenominator() {
        Map<BodyPart, BodyPartHealth> health = healthyBody();
        health.put(BodyPart.HEAD, new BodyPartHealth(120, 60));
        health.put(BodyPart.LEFT_ARM, new BodyPartHealth(90, 45));

        HealthResult result = calculator.calculate(health);

        assertEquals(new BigDecimal("33.33"), result.globalHealthPercent());
        assertFalse(result.dead());
    }

    @Test
    void rejectsMissingBodyParts() {
        assertThrows(IllegalArgumentException.class, () -> calculator.calculate(Map.of()));
    }

    private static Map<BodyPart, BodyPartHealth> healthyBody() {
        Map<BodyPart, BodyPartHealth> health = new EnumMap<>(BodyPart.class);
        health.put(BodyPart.HEAD, new BodyPartHealth(60, 60));
        health.put(BodyPart.NECK, new BodyPartHealth(40, 40));
        health.put(BodyPart.TORSO, new BodyPartHealth(100, 100));
        health.put(BodyPart.LEFT_ARM, new BodyPartHealth(60, 60));
        health.put(BodyPart.RIGHT_ARM, new BodyPartHealth(60, 60));
        health.put(BodyPart.LEFT_LEG, new BodyPartHealth(60, 60));
        health.put(BodyPart.RIGHT_LEG, new BodyPartHealth(60, 60));
        return health;
    }
}
