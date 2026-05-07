package com.pfp.companion.charactersheet.entity;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public final class Character {

    private final UUID id;
    private final Ownership ownership;
    private final Instant createdAt;
    private String name;
    private String image;
    private CharacterInfo info;
    private CharacterStats stats;
    private final Map<SkillName, CharacterSkill> skills;
    private CharacterCondition condition;
    private BlessingInspiration blessings;
    private AdditionalInfo additionalInfo;
    private final Inventory inventory;
    private final EnumMap<EquipmentSlotCode, EquipmentSlot> equipment;
    private final Money money;
    private final Map<UUID, Spell> spells;

    public Character(UUID id, Ownership ownership, Instant createdAt, String name, String image,
            CharacterInfo info, CharacterStats stats, Map<SkillName, CharacterSkill> skills,
            CharacterCondition condition, BlessingInspiration blessings, AdditionalInfo additionalInfo,
            Inventory inventory, Money money, Map<UUID, Spell> spells) {
        this.id = Objects.requireNonNull(id);
        this.ownership = Objects.requireNonNull(ownership);
        this.createdAt = Objects.requireNonNull(createdAt);
        setName(name);
        this.image = image == null ? "" : image;
        this.info = Objects.requireNonNull(info);
        this.stats = Objects.requireNonNull(stats);
        this.skills = new LinkedHashMap<>(skills);
        this.condition = Objects.requireNonNull(condition);
        this.blessings = Objects.requireNonNull(blessings);
        this.additionalInfo = Objects.requireNonNull(additionalInfo);
        this.inventory = Objects.requireNonNull(inventory);
        this.money = Objects.requireNonNull(money);
        this.spells = new LinkedHashMap<>(spells);
        this.equipment = emptyEquipment();
    }

    public static Character createNew(String name, Ownership ownership) {
        Map<SkillName, CharacterSkill> skills = new LinkedHashMap<>();
        for (SkillName skillName : SkillName.values()) {
            skills.put(skillName, new CharacterSkill(skillName, 0));
        }
        Inventory inventory = new Inventory();
        inventory.addSlots(Inventory.SLOTS_PER_ROW);
        return new Character(UUID.randomUUID(), ownership, Instant.now(), name, "",
                CharacterInfo.empty(), CharacterStats.zeroed(), skills, CharacterCondition.initial(),
                BlessingInspiration.empty(), AdditionalInfo.empty(), inventory, Money.empty(),
                Map.of());
    }

    public void equip(EquipmentSlotCode slotCode, UUID itemId) {
        Item item = inventory.requireItem(itemId);
        if (item.type() != ItemType.EQUIPMENT || item.equipmentType() != slotCode.acceptedType()) {
            throw new IllegalArgumentException("item is incompatible with equipment slot");
        }
        boolean alreadyEquipped = equipment.values().stream()
                .anyMatch(slot -> itemId.equals(slot.itemId()));
        if (alreadyEquipped) {
            throw new IllegalArgumentException("item is already equipped");
        }
        equipment.put(slotCode, new EquipmentSlot(slotCode, itemId));
    }

    public void unequip(EquipmentSlotCode slotCode) {
        equipment.put(slotCode, new EquipmentSlot(slotCode, null));
    }

    public void throwAwayItem(UUID itemId) {
        unequipItem(itemId);
        inventory.remove(itemId);
    }

    public int addItem(Item item) {
        return inventory.addToNearestFreeSlot(item);
    }

    public void addInventoryRows(int rowsToAdd) {
        if (rowsToAdd <= 0) {
            throw new IllegalArgumentException("rowsToAdd must be positive");
        }
        inventory.addSlots(Math.multiplyExact(rowsToAdd, Inventory.SLOTS_PER_ROW));
    }

    public void removeInventoryRow() {
        inventory.removeLastRow();
    }

    public void moveInventoryItem(int fromSlotIndex, int toSlotIndex) {
        inventory.moveOrSwap(fromSlotIndex, toSlotIndex);
    }

    public void updateItem(Item item) {
        inventory.requireItem(item.id());
        equipment.values().stream()
                .filter(slot -> item.id().equals(slot.itemId()))
                .forEach(slot -> {
                    if (item.type() != ItemType.EQUIPMENT
                            || item.equipmentType() != slot.code().acceptedType()) {
                        throw new IllegalArgumentException(
                                "updated item is incompatible with its equipment slot");
                    }
                });
        inventory.replace(item);
    }

    public void sellTradeItem(UUID itemId) {
        Item item = inventory.requireItem(itemId);
        if (item.type() != ItemType.TRADE) {
            throw new IllegalArgumentException("only trade item can be sold");
        }
        money.addTradeSale(item.sellPriceBase());
        throwAwayItem(itemId);
    }

    public void updateInfo(String name, CharacterInfo info) {
        setName(name);
        this.info = Objects.requireNonNull(info);
    }

    public void updateImage(String image) {
        this.image = image == null ? "" : image;
    }

    public void updateStats(CharacterStats stats) {
        this.stats = Objects.requireNonNull(stats);
    }

    public void updateSkills(Map<SkillName, Integer> levels) {
        Objects.requireNonNull(levels);
        levels.forEach((name, level) -> skills.put(Objects.requireNonNull(name),
                new CharacterSkill(name, Objects.requireNonNull(level))));
    }

    public void updateCondition(CharacterCondition condition) {
        this.condition = Objects.requireNonNull(condition);
    }

    public void updateBlessings(BlessingInspiration blessings) {
        this.blessings = Objects.requireNonNull(blessings);
    }

    public void updateAdditionalInfo(AdditionalInfo additionalInfo) {
        this.additionalInfo = Objects.requireNonNull(additionalInfo);
    }

    public void selectDisplayCurrency(Currency currency) {
        money.selectDisplayCurrency(currency);
    }

    public void setMoneyAmountBase(BigDecimal amountBase) {
        money.setAmountBase(amountBase);
    }

    public void addSpell(Spell spell) {
        Objects.requireNonNull(spell);
        if (spells.putIfAbsent(spell.id(), spell) != null) {
            throw new IllegalArgumentException("spell already exists");
        }
    }

    public void updateSpell(Spell spell) {
        Objects.requireNonNull(spell);
        if (!spells.containsKey(spell.id())) {
            throw new IllegalArgumentException("unknown spell");
        }
        spells.put(spell.id(), spell);
    }

    public void deleteSpell(UUID spellId) {
        if (spells.remove(spellId) == null) {
            throw new IllegalArgumentException("unknown spell");
        }
    }

    public BigDecimal currentCarryWeight() {
        return inventory.currentWeight();
    }

    public UUID id() {
        return id;
    }

    public Ownership ownership() {
        return ownership;
    }

    public Instant createdAt() {
        return createdAt;
    }

    public String name() {
        return name;
    }

    public String image() {
        return image;
    }

    public CharacterInfo info() {
        return info;
    }

    public CharacterStats stats() {
        return stats;
    }

    public Map<SkillName, CharacterSkill> skills() {
        return Map.copyOf(skills);
    }

    public CharacterCondition condition() {
        return condition;
    }

    public BlessingInspiration blessings() {
        return blessings;
    }

    public AdditionalInfo additionalInfo() {
        return additionalInfo;
    }

    public Inventory inventory() {
        return inventory;
    }

    public Map<EquipmentSlotCode, EquipmentSlot> equipment() {
        return Map.copyOf(equipment);
    }

    public Money money() {
        return money;
    }

    public Map<UUID, Spell> spells() {
        return Map.copyOf(spells);
    }

    private void setName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("character name must not be blank");
        }
        this.name = name;
    }

    private void unequipItem(UUID itemId) {
        equipment.replaceAll((code, slot) -> itemId.equals(slot.itemId())
                ? new EquipmentSlot(code, null) : slot);
    }

    private static EnumMap<EquipmentSlotCode, EquipmentSlot> emptyEquipment() {
        EnumMap<EquipmentSlotCode, EquipmentSlot> equipment = new EnumMap<>(EquipmentSlotCode.class);
        for (EquipmentSlotCode slotCode : EquipmentSlotCode.values()) {
            equipment.put(slotCode, new EquipmentSlot(slotCode, null));
        }
        return equipment;
    }
}
