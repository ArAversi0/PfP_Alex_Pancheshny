package com.pfp.companion.charactersheet.control;

import com.pfp.companion.charactersheet.control.CharacterSheetResponse.AdditionalInfoResponse;
import com.pfp.companion.charactersheet.control.CharacterSheetResponse.BlessingsResponse;
import com.pfp.companion.charactersheet.control.CharacterSheetResponse.BodyHealthResponse;
import com.pfp.companion.charactersheet.control.CharacterSheetResponse.BodyPartHealthResponse;
import com.pfp.companion.charactersheet.control.CharacterSheetResponse.ConditionResponse;
import com.pfp.companion.charactersheet.control.CharacterSheetResponse.EquippedItemResponse;
import com.pfp.companion.charactersheet.control.CharacterSheetResponse.InventoryResponse;
import com.pfp.companion.charactersheet.control.CharacterSheetResponse.InventorySlotResponse;
import com.pfp.companion.charactersheet.control.CharacterSheetResponse.ItemResponse;
import com.pfp.companion.charactersheet.control.CharacterSheetResponse.MoneyResponse;
import com.pfp.companion.charactersheet.control.CharacterSheetResponse.SkillResponse;
import com.pfp.companion.charactersheet.control.CharacterSheetResponse.SpellResponse;
import com.pfp.companion.charactersheet.control.CharacterSheetResponse.StatResponse;
import com.pfp.companion.charactersheet.entity.Character;
import com.pfp.companion.charactersheet.entity.CharacterInfo;
import com.pfp.companion.charactersheet.entity.CharacterSkill;
import com.pfp.companion.charactersheet.entity.EquipmentSlot;
import com.pfp.companion.charactersheet.entity.EquipmentSlotCode;
import com.pfp.companion.charactersheet.entity.InventorySlot;
import com.pfp.companion.charactersheet.entity.Item;
import com.pfp.companion.charactersheet.entity.SkillName;
import com.pfp.companion.charactersheet.entity.Spell;
import com.pfp.companion.charactersheet.entity.StatGroup;
import com.pfp.companion.charactersheet.mediator.CharacterCard;
import com.pfp.gamerules.BodyPart;
import com.pfp.gamerules.BodyPartHealth;
import com.pfp.gamerules.InventoryWeightCalculator;
import com.pfp.gamerules.ProgressionCalculator;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public final class CharacterResponseMapper {

    private final ProgressionCalculator progression = new ProgressionCalculator();
    private final InventoryWeightCalculator inventoryWeight = new InventoryWeightCalculator();

    public CharacterCreatedResponse toCreated(Character character) {
        return new CharacterCreatedResponse(character.id(), character.name());
    }

    public CharacterCardResponse toCard(CharacterCard card) {
        return new CharacterCardResponse(card.id(), card.name(), card.level(), card.className(),
                card.specialization(), card.image());
    }

    public CharacterSummaryResponse toSummary(Character character) {
        return new CharacterSummaryResponse(character.id(), character.name(), character.image(),
                toInfo(character.info()));
    }

    public CharacterSheetResponse toSheet(Character character) {
        BigDecimal currentWeight = character.currentCarryWeight();
        BigDecimal maxCarryWeight = character.condition().maxCarryWeight();
        boolean overweight = inventoryWeight.isOverweight(currentWeight, maxCarryWeight);
        return new CharacterSheetResponse(character.id(), character.name(), character.image(),
                toInfo(character.info()), toStats(character), toSkills(character),
                toCondition(character, currentWeight, overweight),
                toBlessings(character), toAdditionalInfo(character),
                toInventory(character, currentWeight, overweight), toEquipment(character),
                toMoney(character),
                toSpells(character));
    }

    public CharacterInfoResponse toInfo(CharacterInfo info) {
        return new CharacterInfoResponse(info.level(), info.origin(), info.background(),
                info.className(), info.specialization());
    }

    public Map<String, StatResponse> toStats(Character character) {
        Map<String, StatResponse> result = new LinkedHashMap<>();
        for (StatGroup stat : StatGroup.values()) {
            int level = character.stats().level(stat);
            result.put(lowerCamel(stat.name()), new StatResponse(level,
                    progression.rollForLevel(level).display()));
        }
        return Collections.unmodifiableMap(result);
    }

    public List<SkillResponse> toSkills(Character character) {
        List<SkillResponse> result = new ArrayList<>();
        for (SkillName name : SkillName.values()) {
            CharacterSkill skill = character.skills().get(name);
            int statLevel = character.stats().level(skill.statGroup());
            result.add(new SkillResponse(skill.statGroup().name(), name.name(), skill.level(),
                    progression.rollForLevel(skill.level()).display(),
                    progression.effectiveSkillRoll(statLevel, skill.level()).display()));
        }
        return List.copyOf(result);
    }

    public ConditionResponse toCondition(Character character) {
        BigDecimal currentWeight = character.currentCarryWeight();
        boolean overweight = inventoryWeight.isOverweight(currentWeight,
                character.condition().maxCarryWeight());
        return toCondition(character, currentWeight, overweight);
    }

    public BlessingsResponse toBlessings(Character character) {
        return new BlessingsResponse(character.blessings().blessings(),
                character.blessings().inspirations());
    }

    public AdditionalInfoResponse toAdditionalInfo(Character character) {
        return new AdditionalInfoResponse(character.additionalInfo().appearance(),
                character.additionalInfo().detailedOrigin(), character.additionalInfo().allies(),
                character.additionalInfo().notesPrimary(), character.additionalInfo().notesSecondary());
    }

    public InventoryResponse toInventory(Character character) {
        BigDecimal currentWeight = character.currentCarryWeight();
        boolean overweight = inventoryWeight.isOverweight(currentWeight,
                character.condition().maxCarryWeight());
        return toInventory(character, currentWeight, overweight);
    }

    public Map<String, EquippedItemResponse> toEquipment(Character character) {
        Map<String, EquippedItemResponse> result = new LinkedHashMap<>();
        for (EquipmentSlotCode code : EquipmentSlotCode.values()) {
            EquipmentSlot slot = character.equipment().get(code);
            if (slot.itemId() == null) {
                result.put(code.name(), null);
            } else {
                Item item = character.inventory().requireItem(slot.itemId());
                result.put(code.name(), new EquippedItemResponse(item.id(), item.title(), item.image()));
            }
        }
        return Collections.unmodifiableMap(result);
    }

    public MoneyResponse toMoney(Character character) {
        return new MoneyResponse(character.money().amountBase(), character.money().displayAmount(),
                character.money().displayCurrency().name());
    }

    public List<SpellResponse> toSpells(Character character) {
        return character.spells().values().stream()
                .sorted(Comparator.comparing(Spell::name).thenComparing(Spell::id))
                .map(this::toSpell)
                .toList();
    }

    public SpellResponse toSpell(Spell spell) {
        return new SpellResponse(spell.id(), spell.name(), spell.type().name(),
                spell.spellClass().name(), spell.image(), spell.requirements(),
                spell.description());
    }

    public ItemResponse toItem(Item item) {
        return new ItemResponse(item.id(), item.type().name(), item.title(), item.image(),
                item.weight(), item.description(),
                item.equipmentType() == null ? null : item.equipmentType().name(),
                item.sellPriceBase());
    }

    private ConditionResponse toCondition(Character character, BigDecimal currentWeight,
            boolean overweight) {
        Map<BodyPart, BodyPartHealth> hp = character.condition().hp();
        return new ConditionResponse(character.condition().healthResult().globalHealthPercent(),
                character.condition().passiveDefense(),
                progression.passiveDodge(character.stats().level(StatGroup.DEXTERITY)),
                character.condition().movementSpeed(), character.condition().maxCarryWeight(),
                currentWeight, overweight,
                new BodyHealthResponse(toHp(hp.get(BodyPart.HEAD)), toHp(hp.get(BodyPart.NECK)),
                        toHp(hp.get(BodyPart.TORSO)), toHp(hp.get(BodyPart.LEFT_ARM)),
                        toHp(hp.get(BodyPart.RIGHT_ARM)), toHp(hp.get(BodyPart.LEFT_LEG)),
                        toHp(hp.get(BodyPart.RIGHT_LEG))));
    }

    private InventoryResponse toInventory(Character character, BigDecimal currentWeight,
            boolean overweight) {
        List<InventorySlotResponse> slots = character.inventory().slots().values().stream()
                .sorted(Comparator.comparingInt(InventorySlot::slotIndex))
                .map(slot -> new InventorySlotResponse(slot.slotIndex(),
                        slot.itemId() == null ? null : toItem(character.inventory().requireItem(slot.itemId()))))
                .toList();
        return new InventoryResponse(character.condition().maxCarryWeight(), currentWeight,
                overweight, slots);
    }

    private static BodyPartHealthResponse toHp(BodyPartHealth health) {
        return new BodyPartHealthResponse(health.maxHp(), health.currentHp());
    }

    private static String lowerCamel(String enumName) {
        String lower = enumName.toLowerCase();
        StringBuilder result = new StringBuilder();
        boolean upper = false;
        for (char character : lower.toCharArray()) {
            if (character == '_') {
                upper = true;
            } else {
                result.append(upper ? java.lang.Character.toUpperCase(character) : character);
                upper = false;
            }
        }
        return result.toString();
    }
}
