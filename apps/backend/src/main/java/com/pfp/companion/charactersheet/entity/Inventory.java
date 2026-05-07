package com.pfp.companion.charactersheet.entity;

import com.pfp.gamerules.InventoryWeightCalculator;
import java.math.BigDecimal;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

public final class Inventory {

    public static final int SLOTS_PER_ROW = 10;

    private final Map<UUID, Item> items = new LinkedHashMap<>();
    private final Map<Integer, InventorySlot> slots = new LinkedHashMap<>();
    private final InventoryWeightCalculator weightCalculator = new InventoryWeightCalculator();

    public void add(Item item, int slotIndex) {
        if (items.containsKey(item.id())) {
            throw new IllegalArgumentException("item already exists");
        }
        InventorySlot current = slots.get(slotIndex);
        if (current != null && current.itemId() != null) {
            throw new IllegalArgumentException("inventory slot is occupied");
        }
        items.put(item.id(), item);
        slots.put(slotIndex, new InventorySlot(slotIndex, item.id()));
    }

    public void ensureSlot(int slotIndex) {
        slots.putIfAbsent(slotIndex, new InventorySlot(slotIndex, null));
    }

    public void addSlots(int slotsToAdd) {
        if (slotsToAdd <= 0) {
            throw new IllegalArgumentException("slotsToAdd must be positive");
        }
        int firstNewSlot = slots.keySet().stream()
                .max(Comparator.naturalOrder())
                .map(index -> Math.addExact(index, 1))
                .orElse(0);
        for (int offset = 0; offset < slotsToAdd; offset++) {
            ensureSlot(Math.addExact(firstNewSlot, offset));
        }
    }

    public int addToNearestFreeSlot(Item item) {
        int slotIndex = slots.values().stream()
                .filter(slot -> slot.itemId() == null)
                .map(InventorySlot::slotIndex)
                .min(Comparator.naturalOrder())
                .orElseThrow(() -> new IllegalStateException("inventory has no free slots"));
        add(item, slotIndex);
        return slotIndex;
    }

    public void replace(Item item) {
        requireItem(item.id());
        items.put(item.id(), item);
    }

    public void moveOrSwap(int fromSlotIndex, int toSlotIndex) {
        InventorySlot from = requireSlot(fromSlotIndex);
        InventorySlot to = requireSlot(toSlotIndex);
        if (fromSlotIndex == toSlotIndex) {
            return;
        }
        slots.put(fromSlotIndex, new InventorySlot(fromSlotIndex, to.itemId()));
        slots.put(toSlotIndex, new InventorySlot(toSlotIndex, from.itemId()));
    }

    public void removeLastRow() {
        if (slots.size() <= SLOTS_PER_ROW) {
            throw new IllegalArgumentException("cannot remove the only inventory row");
        }
        int lastSlotIndex = slots.keySet().stream()
                .max(Comparator.naturalOrder())
                .orElseThrow(() -> new IllegalStateException("inventory has no slots"));
        int firstSlotIndex = Math.addExact(lastSlotIndex, 1 - SLOTS_PER_ROW);
        boolean lastRowHasItems = slots.values().stream()
                .filter(slot -> slot.slotIndex() >= firstSlotIndex
                        && slot.slotIndex() <= lastSlotIndex)
                .anyMatch(slot -> slot.itemId() != null);
        if (lastRowHasItems) {
            throw new IllegalArgumentException("cannot remove an inventory row with items");
        }
        for (int slotIndex = firstSlotIndex; slotIndex <= lastSlotIndex; slotIndex++) {
            slots.remove(slotIndex);
        }
    }

    public Item remove(UUID itemId) {
        Item item = requireItem(itemId);
        items.remove(itemId);
        slots.replaceAll((index, slot) -> itemId.equals(slot.itemId())
                ? new InventorySlot(index, null) : slot);
        return item;
    }

    public Item requireItem(UUID itemId) {
        Item item = items.get(itemId);
        if (item == null) {
            throw new IllegalArgumentException("unknown inventory item");
        }
        return item;
    }

    private InventorySlot requireSlot(int slotIndex) {
        InventorySlot slot = slots.get(slotIndex);
        if (slot == null) {
            throw new IllegalArgumentException("unknown inventory slot");
        }
        return slot;
    }

    public BigDecimal currentWeight() {
        return weightCalculator.totalWeight(items.values().stream().map(Item::weight).toList());
    }

    public Map<UUID, Item> items() {
        return Map.copyOf(items);
    }

    public Map<Integer, InventorySlot> slots() {
        return Map.copyOf(slots);
    }
}
