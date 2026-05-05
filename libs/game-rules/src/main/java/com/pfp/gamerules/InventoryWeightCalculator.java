package com.pfp.gamerules;

import java.math.BigDecimal;
import java.util.Collection;

public final class InventoryWeightCalculator {

    public BigDecimal totalWeight(Collection<BigDecimal> weights) {
        if (weights == null) {
            throw new IllegalArgumentException("weights must not be null");
        }
        return weights.stream()
                .peek(weight -> requireNonNegative(weight))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public boolean isOverweight(BigDecimal currentWeight, BigDecimal maxCarryWeight) {
        requireNonNegative(currentWeight);
        requireNonNegative(maxCarryWeight);
        return currentWeight.compareTo(maxCarryWeight) > 0;
    }

    private static void requireNonNegative(BigDecimal weight) {
        if (weight == null || weight.signum() < 0) {
            throw new IllegalArgumentException("weight must be non-negative");
        }
    }
}

