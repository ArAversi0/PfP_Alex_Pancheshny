package com.pfp.gamerules;

import java.math.BigDecimal;
import java.math.RoundingMode;

public final class EconomyCalculator {

    private static final int STORAGE_SCALE = 2;

    public BigDecimal canonicalAmount(BigDecimal amount) {
        requireNonNegative(amount, "amount");
        return amount.setScale(STORAGE_SCALE, RoundingMode.HALF_UP);
    }

    public BigDecimal displayAmount(BigDecimal baseAmount, BigDecimal targetRateToBase) {
        requireNonNegative(baseAmount, "baseAmount");
        requirePositive(targetRateToBase, "targetRateToBase");
        return baseAmount.divide(targetRateToBase, 0, RoundingMode.HALF_UP);
    }

    public BigDecimal addTradeSale(BigDecimal currentBaseAmount, BigDecimal salePriceBase) {
        requireNonNegative(currentBaseAmount, "currentBaseAmount");
        requireNonNegative(salePriceBase, "salePriceBase");
        return canonicalAmount(currentBaseAmount.add(salePriceBase));
    }

    private static void requireNonNegative(BigDecimal value, String field) {
        if (value == null || value.signum() < 0) {
            throw new IllegalArgumentException(field + " must be non-negative");
        }
    }

    private static void requirePositive(BigDecimal value, String field) {
        if (value == null || value.signum() <= 0) {
            throw new IllegalArgumentException(field + " must be positive");
        }
    }
}

