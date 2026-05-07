package com.pfp.companion.charactersheet.entity;

import java.math.BigDecimal;

public enum Currency {
    CURRENCY_1("Валюта 1", BigDecimal.ONE),
    CURRENCY_2("Валюта 2", BigDecimal.TEN),
    CURRENCY_3("Валюта 3", BigDecimal.valueOf(100)),
    CURRENCY_4("Валюта 4", BigDecimal.valueOf(1000));

    private final String displayName;
    private final BigDecimal rateToBase;

    Currency(String displayName, BigDecimal rateToBase) {
        this.displayName = displayName;
        this.rateToBase = rateToBase;
    }

    public String displayName() {
        return displayName;
    }

    public BigDecimal rateToBase() {
        return rateToBase;
    }
}

