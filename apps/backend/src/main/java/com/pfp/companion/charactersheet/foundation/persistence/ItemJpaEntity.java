package com.pfp.companion.charactersheet.foundation.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "items")
class ItemJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(name = "public_id", nullable = false, unique = true)
    UUID publicId;

    @Column(nullable = false)
    String title;

    @Column(name = "item_type", nullable = false)
    String itemType;

    @Column(name = "equipment_slot_type")
    String equipmentSlotType;

    @Column(nullable = false)
    String description;

    @Column(name = "image_url", nullable = false)
    String imageUrl;

    @Column(nullable = false)
    BigDecimal weight;

    @Column(name = "sell_price_base_currency")
    BigDecimal sellPriceBaseCurrency;

    protected ItemJpaEntity() {
    }
}

