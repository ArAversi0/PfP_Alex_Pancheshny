package com.pfp.companion.charactersheet.entity;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.UUID;

public record Item(UUID id, ItemType type, String title, String image, BigDecimal weight,
        String description, EquipmentType equipmentType, BigDecimal sellPriceBase) {

    public Item {
        Objects.requireNonNull(id);
        Objects.requireNonNull(type);
        requireText(title, "title");
        Objects.requireNonNull(image);
        requireNonNegative(weight, "weight");
        description = description == null ? "" : description;
        if (type == ItemType.EQUIPMENT && equipmentType == null) {
            throw new IllegalArgumentException("equipment item requires equipmentType");
        }
        if (type != ItemType.EQUIPMENT && equipmentType != null) {
            throw new IllegalArgumentException("only equipment item can have equipmentType");
        }
        if (type == ItemType.TRADE) {
            requireNonNegative(sellPriceBase, "sellPriceBase");
        } else if (sellPriceBase != null) {
            throw new IllegalArgumentException("only trade item can have sellPriceBase");
        }
    }

    private static void requireText(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(field + " must not be blank");
        }
    }

    private static void requireNonNegative(BigDecimal value, String field) {
        if (value == null || value.signum() < 0) {
            throw new IllegalArgumentException(field + " must be non-negative");
        }
    }
}
