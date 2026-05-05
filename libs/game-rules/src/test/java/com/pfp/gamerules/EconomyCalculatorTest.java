package com.pfp.gamerules;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class EconomyCalculatorTest {

    private final EconomyCalculator calculator = new EconomyCalculator();

    @Test
    void storesCanonicalMoneyWithTwoDecimalPlaces() {
        assertEquals(new BigDecimal("12.35"), calculator.canonicalAmount(new BigDecimal("12.345")));
    }

    @Test
    void roundsPlayerFacingDisplayToInteger() {
        assertEquals(new BigDecimal("13"),
                calculator.displayAmount(new BigDecimal("126.00"), BigDecimal.TEN));
    }

    @Test
    void addsTradeSaleInBaseCurrency() {
        assertEquals(new BigDecimal("112.50"),
                calculator.addTradeSale(new BigDecimal("100.00"), new BigDecimal("12.50")));
    }

    @Test
    void rejectsNegativeMoney() {
        assertThrows(IllegalArgumentException.class,
                () -> calculator.canonicalAmount(new BigDecimal("-0.01")));
    }
}

