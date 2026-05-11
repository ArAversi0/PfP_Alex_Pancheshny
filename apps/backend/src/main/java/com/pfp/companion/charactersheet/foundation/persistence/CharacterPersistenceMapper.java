package com.pfp.companion.charactersheet.foundation.persistence;

import com.pfp.companion.charactersheet.entity.AdditionalInfo;
import com.pfp.companion.charactersheet.entity.BlessingInspiration;
import com.pfp.companion.charactersheet.entity.Character;
import com.pfp.companion.charactersheet.entity.CharacterCondition;
import com.pfp.companion.charactersheet.entity.CharacterInfo;
import com.pfp.companion.charactersheet.entity.CharacterSkill;
import com.pfp.companion.charactersheet.entity.CharacterStats;
import com.pfp.companion.charactersheet.entity.Currency;
import com.pfp.companion.charactersheet.entity.EquipmentSlotCode;
import com.pfp.companion.charactersheet.entity.EquipmentType;
import com.pfp.companion.charactersheet.entity.Inventory;
import com.pfp.companion.charactersheet.entity.Item;
import com.pfp.companion.charactersheet.entity.ItemType;
import com.pfp.companion.charactersheet.entity.Money;
import com.pfp.companion.charactersheet.entity.Ownership;
import com.pfp.companion.charactersheet.entity.SkillName;
import com.pfp.companion.charactersheet.entity.Spell;
import com.pfp.companion.charactersheet.entity.SpellClass;
import com.pfp.companion.charactersheet.entity.SpellType;
import com.pfp.companion.charactersheet.entity.StatGroup;
import com.pfp.gamerules.BodyPart;
import com.pfp.gamerules.BodyPartHealth;
import com.pfp.gamerules.ProgressionCalculator;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
class CharacterPersistenceMapper {

    private final ProgressionCalculator progressionCalculator = new ProgressionCalculator();

    CharacterJpaEntity toJpa(Character source, CharacterOwnerJpaEntity user, CurrencyJpaEntity currency) {
        CharacterJpaEntity target = new CharacterJpaEntity();
        target.publicId = source.id();
        target.user = user;
        target.name = source.name();
        target.image = source.image();
        target.createdAt = source.createdAt();
        target.info = infoToJpa(source.info(), target);
        target.stats = statsToJpa(source, target);
        target.condition = conditionToJpa(source, target);
        target.blessings = blessingsToJpa(source.blessings(), target);
        target.additionalInfo = additionalInfoToJpa(source.additionalInfo(), target);
        target.inventory = inventoryToJpa(source.inventory(), target);
        target.equipmentSlots.addAll(equipmentToJpa(source, target));
        target.money = moneyToJpa(source.money(), target, currency);
        target.spells.addAll(spellsToJpa(source, target));
        return target;
    }

    Character toDomain(CharacterJpaEntity source) {
        Inventory inventory = inventoryToDomain(source.inventory);
        Character character = new Character(source.publicId, Ownership.authenticated(source.user.publicId()),
                source.createdAt, source.name, source.image, infoToDomain(source.info),
                statsToDomain(source.stats), skillsToDomain(source.stats), conditionToDomain(source.condition),
                blessingsToDomain(source.blessings), additionalInfoToDomain(source.additionalInfo), inventory,
                moneyToDomain(source.money), spellsToDomain(source.spells));
        source.equipmentSlots.stream()
                .filter(slot -> slot.item != null)
                .forEach(slot -> character.equip(EquipmentSlotCode.valueOf(slot.slotCode), slot.item.publicId));
        return character;
    }

    private CharacterInfoJpaEntity infoToJpa(CharacterInfo source, CharacterJpaEntity character) {
        CharacterInfoJpaEntity target = new CharacterInfoJpaEntity();
        target.character = character;
        target.level = source.level();
        target.origin = source.origin();
        target.background = source.background();
        target.className = source.className();
        target.specialization = source.specialization();
        return target;
    }

    private CharacterStatsJpaEntity statsToJpa(Character source, CharacterJpaEntity character) {
        CharacterStatsJpaEntity target = new CharacterStatsJpaEntity();
        target.character = character;
        target.strength = source.stats().level(StatGroup.STRENGTH);
        target.dexterity = source.stats().level(StatGroup.DEXTERITY);
        target.stamina = source.stats().level(StatGroup.STAMINA);
        target.intelligence = source.stats().level(StatGroup.INTELLIGENCE);
        target.charisma = source.stats().level(StatGroup.CHARISMA);
        target.luck = source.stats().level(StatGroup.LUCK);
        target.mind = source.stats().level(StatGroup.MIND);
        source.skills().values().forEach(skill -> {
            CharacterSkillJpaEntity skillJpa = new CharacterSkillJpaEntity();
            skillJpa.stats = target;
            skillJpa.statGroup = skill.statGroup().name();
            skillJpa.skillName = skill.name().name();
            skillJpa.skillLevel = skill.level();
            target.skills.add(skillJpa);
        });
        return target;
    }

    private CharacterConditionJpaEntity conditionToJpa(Character source, CharacterJpaEntity character) {
        CharacterConditionJpaEntity target = new CharacterConditionJpaEntity();
        target.character = character;
        target.globalHealthPercent = source.condition().healthResult().globalHealthPercent();
        hpToJpa(target, source.condition());
        target.passiveDefense = source.condition().passiveDefense();
        target.passiveDodge = progressionCalculator.passiveDodge(source.stats().level(StatGroup.DEXTERITY));
        target.movementSpeed = source.condition().movementSpeed();
        target.maxCarryWeight = source.condition().maxCarryWeight();
        target.currentCarryWeight = source.currentCarryWeight();
        return target;
    }

    private static void hpToJpa(CharacterConditionJpaEntity target, CharacterCondition source) {
        target.headMaxHp = hp(source, BodyPart.HEAD).maxHp();
        target.headCurrentHp = hp(source, BodyPart.HEAD).currentHp();
        target.neckMaxHp = hp(source, BodyPart.NECK).maxHp();
        target.neckCurrentHp = hp(source, BodyPart.NECK).currentHp();
        target.torsoMaxHp = hp(source, BodyPart.TORSO).maxHp();
        target.torsoCurrentHp = hp(source, BodyPart.TORSO).currentHp();
        target.leftArmMaxHp = hp(source, BodyPart.LEFT_ARM).maxHp();
        target.leftArmCurrentHp = hp(source, BodyPart.LEFT_ARM).currentHp();
        target.rightArmMaxHp = hp(source, BodyPart.RIGHT_ARM).maxHp();
        target.rightArmCurrentHp = hp(source, BodyPart.RIGHT_ARM).currentHp();
        target.leftLegMaxHp = hp(source, BodyPart.LEFT_LEG).maxHp();
        target.leftLegCurrentHp = hp(source, BodyPart.LEFT_LEG).currentHp();
        target.rightLegMaxHp = hp(source, BodyPart.RIGHT_LEG).maxHp();
        target.rightLegCurrentHp = hp(source, BodyPart.RIGHT_LEG).currentHp();
    }

    private InventoryJpaEntity inventoryToJpa(Inventory source, CharacterJpaEntity character) {
        InventoryJpaEntity target = new InventoryJpaEntity();
        target.character = character;
        source.slots().values().forEach(slot -> {
            InventorySlotJpaEntity slotJpa = new InventorySlotJpaEntity();
            slotJpa.inventory = target;
            slotJpa.slotIndex = slot.slotIndex();
            if (slot.itemId() != null) {
                slotJpa.item = itemToJpa(source.requireItem(slot.itemId()));
            }
            target.slots.add(slotJpa);
        });
        return target;
    }

    private static ItemJpaEntity itemToJpa(Item source) {
        ItemJpaEntity target = new ItemJpaEntity();
        target.publicId = source.id();
        target.title = source.title();
        target.itemType = source.type().name();
        target.equipmentSlotType = source.equipmentType() == null ? null : source.equipmentType().name();
        target.description = source.description();
        target.imageUrl = source.image();
        target.weight = source.weight();
        target.sellPriceBaseCurrency = source.sellPriceBase();
        return target;
    }

    private static java.util.List<EquipmentSlotJpaEntity> equipmentToJpa(
            Character source, CharacterJpaEntity character) {
        Map<UUID, ItemJpaEntity> items = new LinkedHashMap<>();
        character.inventory.slots.stream().filter(slot -> slot.item != null)
                .forEach(slot -> items.put(slot.item.publicId, slot.item));
        return source.equipment().values().stream().map(slot -> {
            EquipmentSlotJpaEntity target = new EquipmentSlotJpaEntity();
            target.character = character;
            target.slotCode = slot.code().name();
            target.item = slot.itemId() == null ? null : items.get(slot.itemId());
            return target;
        }).toList();
    }

    private static MoneyJpaEntity moneyToJpa(Money source, CharacterJpaEntity character,
            CurrencyJpaEntity currency) {
        MoneyJpaEntity target = new MoneyJpaEntity();
        target.character = character;
        target.currency = currency;
        target.amount = source.amountBase().divide(source.displayCurrency().rateToBase(), 2, RoundingMode.HALF_UP);
        return target;
    }

    private static java.util.List<SpellJpaEntity> spellsToJpa(Character source, CharacterJpaEntity character) {
        return source.spells().values().stream().map(spell -> {
            SpellJpaEntity target = new SpellJpaEntity();
            target.publicId = spell.id();
            target.character = character;
            target.spellName = spell.name();
            target.imageUrl = spell.image();
            target.spellType = spell.type().name();
            target.spellClass = spell.spellClass().name();
            target.requirements = spell.requirements();
            target.description = spell.description();
            return target;
        }).toList();
    }

    private static CharacterInfo infoToDomain(CharacterInfoJpaEntity source) {
        return new CharacterInfo(source.level, source.origin, source.background, source.className,
                source.specialization);
    }

    private static CharacterStats statsToDomain(CharacterStatsJpaEntity source) {
        return new CharacterStats(Map.of(
                StatGroup.STRENGTH, source.strength, StatGroup.DEXTERITY, source.dexterity,
                StatGroup.STAMINA, source.stamina, StatGroup.INTELLIGENCE, source.intelligence,
                StatGroup.CHARISMA, source.charisma, StatGroup.LUCK, source.luck, StatGroup.MIND, source.mind));
    }

    private static Map<SkillName, CharacterSkill> skillsToDomain(CharacterStatsJpaEntity source) {
        Map<SkillName, CharacterSkill> skills = new LinkedHashMap<>();
        source.skills.forEach(skill -> {
            SkillName name = SkillName.valueOf(skill.skillName);
            skills.put(name, new CharacterSkill(name, skill.skillLevel));
        });
        return skills;
    }

    private static CharacterCondition conditionToDomain(CharacterConditionJpaEntity source) {
        EnumMap<BodyPart, BodyPartHealth> hp = new EnumMap<>(BodyPart.class);
        hp.put(BodyPart.HEAD, new BodyPartHealth(source.headMaxHp, source.headCurrentHp));
        hp.put(BodyPart.NECK, new BodyPartHealth(source.neckMaxHp, source.neckCurrentHp));
        hp.put(BodyPart.TORSO, new BodyPartHealth(source.torsoMaxHp, source.torsoCurrentHp));
        hp.put(BodyPart.LEFT_ARM, new BodyPartHealth(source.leftArmMaxHp, source.leftArmCurrentHp));
        hp.put(BodyPart.RIGHT_ARM, new BodyPartHealth(source.rightArmMaxHp, source.rightArmCurrentHp));
        hp.put(BodyPart.LEFT_LEG, new BodyPartHealth(source.leftLegMaxHp, source.leftLegCurrentHp));
        hp.put(BodyPart.RIGHT_LEG, new BodyPartHealth(source.rightLegMaxHp, source.rightLegCurrentHp));
        return new CharacterCondition(hp, source.passiveDefense, source.movementSpeed, source.maxCarryWeight);
    }

    private static Inventory inventoryToDomain(InventoryJpaEntity source) {
        Inventory inventory = new Inventory();
        source.slots.forEach(slot -> {
            inventory.ensureSlot(slot.slotIndex);
            if (slot.item != null) {
                inventory.add(itemToDomain(slot.item), slot.slotIndex);
            }
        });
        return inventory;
    }

    private static Item itemToDomain(ItemJpaEntity source) {
        return new Item(source.publicId, ItemType.valueOf(source.itemType), source.title, source.imageUrl,
                source.weight, source.description,
                source.equipmentSlotType == null ? null : EquipmentType.valueOf(source.equipmentSlotType),
                source.sellPriceBaseCurrency);
    }

    private static Money moneyToDomain(MoneyJpaEntity source) {
        Currency currency = Currency.valueOf(source.currency.code());
        return new Money(source.amount.multiply(currency.rateToBase()), currency);
    }

    private static Map<UUID, Spell> spellsToDomain(java.util.List<SpellJpaEntity> source) {
        Map<UUID, Spell> spells = new LinkedHashMap<>();
        source.forEach(spell -> spells.put(spell.publicId,
                new Spell(spell.publicId, spell.spellName, SpellType.valueOf(spell.spellType),
                        SpellClass.valueOf(spell.spellClass), spell.imageUrl, spell.requirements,
                        spell.description)));
        return spells;
    }

    private static BlessingInspirationJpaEntity blessingsToJpa(
            BlessingInspiration source, CharacterJpaEntity character) {
        BlessingInspirationJpaEntity target = new BlessingInspirationJpaEntity();
        target.character = character;
        target.blessings = source.blessings();
        target.inspirations = source.inspirations();
        return target;
    }

    private static BlessingInspiration blessingsToDomain(BlessingInspirationJpaEntity source) {
        return new BlessingInspiration(source.blessings, source.inspirations);
    }

    private static AdditionalInfoJpaEntity additionalInfoToJpa(
            AdditionalInfo source, CharacterJpaEntity character) {
        AdditionalInfoJpaEntity target = new AdditionalInfoJpaEntity();
        target.character = character;
        target.appearance = source.appearance();
        target.detailedOrigin = source.detailedOrigin();
        target.alliesAndOrganizations = source.allies();
        target.notesPrimary = source.notesPrimary();
        target.notesSecondary = source.notesSecondary();
        return target;
    }

    private static AdditionalInfo additionalInfoToDomain(AdditionalInfoJpaEntity source) {
        return new AdditionalInfo(source.appearance, source.detailedOrigin, source.alliesAndOrganizations,
                source.notesPrimary, source.notesSecondary);
    }

    private static BodyPartHealth hp(CharacterCondition source, BodyPart part) {
        return source.hp().get(part);
    }
}
