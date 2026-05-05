package com.pfp.gamerules;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.EnumSet;
import java.util.Map;

public final class HealthCalculator {

    private static final MathContext CONTEXT = new MathContext(12, RoundingMode.HALF_UP);
    private static final BigDecimal ONE_HUNDRED = BigDecimal.valueOf(100);
    public HealthResult calculate(Map<BodyPart, BodyPartHealth> healthByPart) {
        requireAllBodyParts(healthByPart);
        BigDecimal totalDamagePercent = healthByPart.entrySet().stream()
                .map(entry -> damagePercent(entry.getKey(), entry.getValue()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal remaining = ONE_HUNDRED.subtract(totalDamagePercent)
                .max(BigDecimal.ZERO)
                .setScale(2, RoundingMode.HALF_UP);
        long destroyedLimbs = healthByPart.entrySet().stream()
                .filter(entry -> isLimb(entry.getKey()))
                .filter(entry -> entry.getValue().currentHp() == 0)
                .count();
        boolean criticalZoneDestroyed = healthByPart.get(BodyPart.HEAD).currentHp() == 0
                || healthByPart.get(BodyPart.NECK).currentHp() == 0
                || healthByPart.get(BodyPart.TORSO).currentHp() == 0;
        return new HealthResult(remaining,
                criticalZoneDestroyed || destroyedLimbs >= 3 || remaining.signum() == 0);
    }

    private static BigDecimal damagePercent(BodyPart bodyPart, BodyPartHealth health) {
        if (health.maxHp() == 0) {
            return BigDecimal.ZERO;
        }
        int denominator = isLimb(bodyPart) ? health.maxHp() * 3 : health.maxHp();
        return BigDecimal.valueOf(health.damage())
                .multiply(ONE_HUNDRED)
                .divide(BigDecimal.valueOf(denominator), CONTEXT);
    }

    private static boolean isLimb(BodyPart part) {
        return part == BodyPart.LEFT_ARM || part == BodyPart.RIGHT_ARM
                || part == BodyPart.LEFT_LEG || part == BodyPart.RIGHT_LEG;
    }

    private static void requireAllBodyParts(Map<BodyPart, BodyPartHealth> healthByPart) {
        if (healthByPart == null || !healthByPart.keySet().containsAll(EnumSet.allOf(BodyPart.class))) {
            throw new IllegalArgumentException("healthByPart must contain all body parts");
        }
    }
}
