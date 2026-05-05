package com.pfp.gamerules;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;

class InventoryWeightCalculatorTest {

    private final InventoryWeightCalculator calculator = new InventoryWeightCalculator();

    @Test
    void sumsDecimalWeights() {
        assertEquals(new BigDecimal("3.75"),
                calculator.totalWeight(List.of(new BigDecimal("1.25"), new BigDecimal("2.50"))));
    }

    @Test
    void reportsOverweightOnlyWhenWeightExceedsMaximum() {
        assertFalse(calculator.isOverweight(new BigDecimal("3.75"), new BigDecimal("3.75")));
        assertTrue(calculator.isOverweight(new BigDecimal("3.76"), new BigDecimal("3.75")));
    }
}

