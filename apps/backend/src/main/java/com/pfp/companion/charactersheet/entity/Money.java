package com.pfp.companion.charactersheet.entity;

import com.pfp.gamerules.EconomyCalculator;
import java.math.BigDecimal;
import java.util.Objects;

public final class Money {

    private final EconomyCalculator calculator = new EconomyCalculator();
    private BigDecimal amountBase;
    private Currency displayCurrency;

    public Money(BigDecimal amountBase, Currency displayCurrency) {
        this.amountBase = calculator.canonicalAmount(amountBase);
        this.displayCurrency = Objects.requireNonNull(displayCurrency);
    }

    public static Money empty() {
        return new Money(BigDecimal.ZERO, Currency.CURRENCY_1);
    }

    public BigDecimal amountBase() {
        return amountBase;
    }

    public Currency displayCurrency() {
        return displayCurrency;
    }

    public BigDecimal displayAmount() {
        return calculator.displayAmount(amountBase, displayCurrency.rateToBase());
    }

    public void selectDisplayCurrency(Currency currency) {
        displayCurrency = Objects.requireNonNull(currency);
    }

    public void setAmountBase(BigDecimal amountBase) {
        this.amountBase = calculator.canonicalAmount(amountBase);
    }

    public void addTradeSale(BigDecimal salePriceBase) {
        amountBase = calculator.addTradeSale(amountBase, salePriceBase);
    }
}
