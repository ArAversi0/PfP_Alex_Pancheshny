package com.pfp.desktop.foundation.api;

import com.pfp.desktop.foundation.json.LocalCharacterSheet;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public record AccountCharacterSheet(
        UUID id,
        String name,
        String imageUrl,
        InfoResponse info,
        Map<String, StatResponse> stats,
        List<SkillResponse> skills,
        ConditionResponse condition,
        BlessingsResponse blessings,
        AdditionalInfoResponse additionalInfo,
        InventoryResponse inventory,
        Map<String, EquippedItemResponse> equipment,
        MoneyResponse money,
        List<SpellResponse> spells
) {
    public LocalCharacterSheet toLocalSheet() {
        return new LocalCharacterSheet(
                id.toString(),
                text(name),
                text(imageUrl),
                new LocalCharacterSheet.Info(
                        info == null ? 1 : info.level(),
                        info == null ? "" : info.origin(),
                        info == null ? "" : info.background(),
                        info == null ? "" : info.className(),
                        info == null ? "" : info.specialization()
                ),
                new LocalCharacterSheet.Stats(
                        statLevel("STRENGTH", "strength"),
                        statLevel("DEXTERITY", "dexterity"),
                        statLevel("STAMINA", "stamina"),
                        statLevel("INTELLIGENCE", "intelligence"),
                        statLevel("CHARISMA", "charisma"),
                        statLevel("LUCK", "luck"),
                        statLevel("MIND", "mind")
                ),
                skills == null ? List.of() : skills.stream()
                        .map(skill -> new LocalCharacterSheet.Skill(skill.statGroup(), skill.skillName(), skill.level()))
                        .toList(),
                new LocalCharacterSheet.Condition(toBodyHealth(), condition == null ? 0 : condition.passiveDefense(),
                        condition == null ? BigDecimal.ZERO : condition.movementSpeed(),
                        condition == null ? BigDecimal.ZERO : condition.maxCarryWeight()),
                new LocalCharacterSheet.Blessings(
                        blessings == null ? 0 : blessings.blessings(),
                        blessings == null ? 0 : blessings.inspirations()
                ),
                new LocalCharacterSheet.Money(
                        money == null ? BigDecimal.ZERO : money.amountBase(),
                        money == null ? "CURRENCY_1" : money.displayCurrency()
                ),
                toInventory(),
                toEquipment(),
                spells == null ? List.of() : spells.stream()
                        .map(spell -> new LocalCharacterSheet.SpellPreview(
                                spell.id().toString(),
                                spell.name(),
                                spell.type(),
                                spell.spellClass(),
                                spell.requirements(),
                                spell.imageUrl(),
                                spell.description()
                        ))
                        .toList(),
                new LocalCharacterSheet.AdditionalInfo(
                        additionalInfo == null ? "" : additionalInfo.appearance(),
                        additionalInfo == null ? "" : additionalInfo.detailedOrigin(),
                        additionalInfo == null ? "" : additionalInfo.allies(),
                        additionalInfo == null ? "" : additionalInfo.notesPrimary(),
                        additionalInfo == null ? "" : additionalInfo.notesSecondary()
                )
        );
    }

    private int statLevel(String upperKey, String lowerKey) {
        if (stats == null) {
            return 0;
        }
        StatResponse stat = stats.get(upperKey);
        if (stat == null) {
            stat = stats.get(lowerKey);
        }
        return stat == null ? 0 : stat.level();
    }

    private LocalCharacterSheet.BodyHealth toBodyHealth() {
        BodyHealthResponse hp = condition == null ? null : condition.hp();
        return new LocalCharacterSheet.BodyHealth(
                toBodyPart(hp == null ? null : hp.head(), 60),
                toBodyPart(hp == null ? null : hp.neck(), 40),
                toBodyPart(hp == null ? null : hp.torso(), 100),
                toBodyPart(hp == null ? null : hp.leftArm(), 60),
                toBodyPart(hp == null ? null : hp.rightArm(), 60),
                toBodyPart(hp == null ? null : hp.leftLeg(), 60),
                toBodyPart(hp == null ? null : hp.rightLeg(), 60)
        );
    }

    private LocalCharacterSheet.BodyPartHealth toBodyPart(BodyPartHealthResponse part, int fallbackMax) {
        if (part == null) {
            return new LocalCharacterSheet.BodyPartHealth(fallbackMax, fallbackMax);
        }
        return new LocalCharacterSheet.BodyPartHealth(part.current(), part.max());
    }

    private LocalCharacterSheet.Inventory toInventory() {
        if (inventory == null || inventory.slots() == null) {
            List<LocalCharacterSheet.InventorySlot> emptySlots = new ArrayList<>();
            for (int index = 0; index < 10; index++) {
                emptySlots.add(new LocalCharacterSheet.InventorySlot(index, ""));
            }
            return new LocalCharacterSheet.Inventory(List.of(), emptySlots);
        }
        Map<String, LocalCharacterSheet.InventoryItem> items = new LinkedHashMap<>();
        List<LocalCharacterSheet.InventorySlot> slots = new ArrayList<>();
        for (InventorySlotResponse slot : inventory.slots()) {
            ItemResponse item = slot.item();
            String itemId = "";
            if (item != null) {
                itemId = item.id().toString();
                items.putIfAbsent(itemId, new LocalCharacterSheet.InventoryItem(
                        itemId,
                        item.type(),
                        item.title(),
                        item.imageUrl(),
                        item.weight(),
                        item.description(),
                        item.equipmentType(),
                        item.sellPriceBase()
                ));
            }
            slots.add(new LocalCharacterSheet.InventorySlot(slot.slotIndex(), itemId));
        }
        return new LocalCharacterSheet.Inventory(new ArrayList<>(items.values()), slots);
    }

    private List<LocalCharacterSheet.EquipmentSlot> toEquipment() {
        if (equipment == null) {
            return List.of();
        }
        return equipment.entrySet().stream()
                .map(entry -> new LocalCharacterSheet.EquipmentSlot(entry.getKey(),
                        entry.getValue() == null ? "" : entry.getValue().itemId().toString()))
                .toList();
    }

    private static String text(String value) {
        return value == null ? "" : value;
    }

    public record InfoResponse(int level, String origin, String background, String className, String specialization) {
    }

    public record StatResponse(int level, String roll) {
    }

    public record SkillResponse(String statGroup, String skillName, int level, String skillRoll, String effectiveRoll) {
    }

    public record ConditionResponse(BigDecimal globalHealthPercent, int passiveDefense, int passiveDodge,
                                    BigDecimal movementSpeed, BigDecimal maxCarryWeight,
                                    BigDecimal currentCarryWeight, boolean overweight, BodyHealthResponse hp) {
    }

    public record BodyHealthResponse(BodyPartHealthResponse head, BodyPartHealthResponse neck,
                                     BodyPartHealthResponse torso, BodyPartHealthResponse leftArm,
                                     BodyPartHealthResponse rightArm, BodyPartHealthResponse leftLeg,
                                     BodyPartHealthResponse rightLeg) {
    }

    public record BodyPartHealthResponse(int max, int current) {
    }

    public record BlessingsResponse(int blessings, int inspirations) {
    }

    public record AdditionalInfoResponse(String appearance, String detailedOrigin, String allies,
                                         String notesPrimary, String notesSecondary) {
    }

    public record InventoryResponse(BigDecimal maxCarryWeight, BigDecimal currentCarryWeight,
                                    boolean overweight, List<InventorySlotResponse> slots) {
    }

    public record InventorySlotResponse(int slotIndex, ItemResponse item) {
    }

    public record ItemResponse(UUID id, String type, String title, String imageUrl,
                               BigDecimal weight, String description, String equipmentType,
                               BigDecimal sellPriceBase) {
    }

    public record EquippedItemResponse(UUID itemId, String title, String imageUrl) {
    }

    public record MoneyResponse(BigDecimal amountBase, BigDecimal displayAmount, String displayCurrency) {
    }

    public record SpellResponse(UUID id, String name, String type, String spellClass,
                                String imageUrl, String requirements, String description) {
    }
}
