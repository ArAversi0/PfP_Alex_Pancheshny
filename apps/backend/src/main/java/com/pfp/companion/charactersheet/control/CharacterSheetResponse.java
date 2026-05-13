package com.pfp.companion.charactersheet.control;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public record CharacterSheetResponse(UUID id, String name, String imageUrl,
        CharacterInfoResponse info, Map<String, StatResponse> stats, List<SkillResponse> skills,
        ConditionResponse condition, BlessingsResponse blessings,
        AdditionalInfoResponse additionalInfo, InventoryResponse inventory,
        Map<String, EquippedItemResponse> equipment, MoneyResponse money,
        List<SpellResponse> spells) {

    public record StatResponse(int level, String roll) {
    }

    public record SkillResponse(String statGroup, String skillName, int level, String skillRoll,
            String effectiveRoll) {
    }

    public record ConditionResponse(BigDecimal globalHealthPercent, int passiveDefense,
            int passiveDodge, BigDecimal movementSpeed, BigDecimal maxCarryWeight,
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

    public record MoneyResponse(BigDecimal amountBase, BigDecimal displayAmount,
            String displayCurrency) {
    }

    public record SpellResponse(UUID id, String name, String type, String spellClass,
            String imageUrl, String requirements, String description) {
    }
}
