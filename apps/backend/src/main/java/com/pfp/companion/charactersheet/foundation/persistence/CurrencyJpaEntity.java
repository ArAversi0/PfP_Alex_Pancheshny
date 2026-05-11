package com.pfp.companion.charactersheet.foundation.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;

@Entity
@Table(name = "currencies")
class CurrencyJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(nullable = false, unique = true)
    String code;

    @Column(name = "rate_to_base", nullable = false)
    BigDecimal rateToBase;

    String code() {
        return code;
    }

    protected CurrencyJpaEntity() {
    }
}
