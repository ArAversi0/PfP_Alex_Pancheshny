package com.pfp.companion.charactersheet.entity;

import java.util.UUID;

public record InventorySlot(int slotIndex, UUID itemId) {

    public InventorySlot {
        if (slotIndex < 0) {
            throw new IllegalArgumentException("slotIndex must be non-negative");
        }
    }
}

