package com.pfp.companion.charactersheet.entity;

import com.pfp.gamerules.BodyPart;
import com.pfp.gamerules.BodyPartHealth;
import com.pfp.gamerules.HealthCalculator;
import com.pfp.gamerules.HealthResult;
import java.math.BigDecimal;
import java.util.EnumMap;
import java.util.Map;

public final class CharacterCondition {

    private final EnumMap<BodyPart, BodyPartHealth> hp;
    private int passiveDefense;
    private BigDecimal movementSpeed;
    private BigDecimal maxCarryWeight;

    public CharacterCondition(Map<BodyPart, BodyPartHealth> hp, int passiveDefense,
            BigDecimal movementSpeed, BigDecimal maxCarryWeight) {
        this.hp = new EnumMap<>(hp);
        new HealthCalculator().calculate(this.hp);
        if (passiveDefense < 0) {
            throw new IllegalArgumentException("passiveDefense must be non-negative");
        }
        requireNonNegative(movementSpeed, "movementSpeed");
        requireNonNegative(maxCarryWeight, "maxCarryWeight");
        this.passiveDefense = passiveDefense;
        this.movementSpeed = movementSpeed;
        this.maxCarryWeight = maxCarryWeight;
    }

    public static CharacterCondition initial() {
        EnumMap<BodyPart, BodyPartHealth> hp = new EnumMap<>(BodyPart.class);
        hp.put(BodyPart.HEAD, new BodyPartHealth(60, 60));
        hp.put(BodyPart.NECK, new BodyPartHealth(40, 40));
        hp.put(BodyPart.TORSO, new BodyPartHealth(100, 100));
        hp.put(BodyPart.LEFT_ARM, new BodyPartHealth(60, 60));
        hp.put(BodyPart.RIGHT_ARM, new BodyPartHealth(60, 60));
        hp.put(BodyPart.LEFT_LEG, new BodyPartHealth(60, 60));
        hp.put(BodyPart.RIGHT_LEG, new BodyPartHealth(60, 60));
        return new CharacterCondition(hp, 0, BigDecimal.ZERO, BigDecimal.ZERO);
    }

    public Map<BodyPart, BodyPartHealth> hp() {
        return Map.copyOf(hp);
    }

    public int passiveDefense() {
        return passiveDefense;
    }

    public BigDecimal movementSpeed() {
        return movementSpeed;
    }

    public BigDecimal maxCarryWeight() {
        return maxCarryWeight;
    }

    public HealthResult healthResult() {
        return new HealthCalculator().calculate(hp);
    }

    public void updateHp(BodyPart bodyPart, BodyPartHealth health) {
        hp.put(bodyPart, health);
    }

    private static void requireNonNegative(BigDecimal value, String field) {
        if (value == null || value.signum() < 0) {
            throw new IllegalArgumentException(field + " must be non-negative");
        }
    }
}

