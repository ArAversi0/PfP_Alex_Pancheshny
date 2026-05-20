package com.pfp.companion.charactertransfer.mediator;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.pfp.companion.charactersheet.entity.AdditionalInfo;
import com.pfp.companion.charactersheet.entity.BlessingInspiration;
import com.pfp.companion.charactersheet.entity.Character;
import com.pfp.companion.charactersheet.entity.CharacterCondition;
import com.pfp.companion.charactersheet.entity.CharacterInfo;
import com.pfp.companion.charactersheet.entity.CharacterSkill;
import com.pfp.companion.charactersheet.entity.CharacterStats;
import com.pfp.companion.charactersheet.entity.Currency;
import com.pfp.companion.charactersheet.entity.EquipmentSlot;
import com.pfp.companion.charactersheet.entity.EquipmentSlotCode;
import com.pfp.companion.charactersheet.entity.EquipmentType;
import com.pfp.companion.charactersheet.entity.Inventory;
import com.pfp.companion.charactersheet.entity.InventorySlot;
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
import java.math.BigDecimal;
import java.time.Instant;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public final class CharacterJsonTransferService {

    public static final String SCHEMA_VERSION = "1.0";

    private final ObjectMapper objectMapper;

    public CharacterJsonTransferService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public String exportCharacter(Character character) {
        ObjectNode root = objectMapper.createObjectNode();
        root.put("schemaVersion", SCHEMA_VERSION);
        root.set("character", writeCharacter(character));
        try {
            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(root);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("could not export character", exception);
        }
    }

    public Character importAsNewLocalGuest(String json) {
        return importAsNew(json, Ownership.localGuest());
    }

    public Character importAsNew(String json, Ownership ownership) {
        try {
            JsonNode root = objectMapper.readTree(json);
            requireText(root, "schemaVersion", SCHEMA_VERSION);
            return readCharacter(required(root, "character"), ownership);
        } catch (JsonProcessingException exception) {
            throw new IllegalArgumentException("invalid character JSON", exception);
        }
    }

    private ObjectNode writeCharacter(Character character) {
        ObjectNode node = objectMapper.createObjectNode();
        node.put("name", character.name());
        node.put("image", character.image());
        node.set("info", writeInfo(character.info()));
        node.set("stats", writeStats(character.stats()));
        node.set("skills", writeSkills(character.skills()));
        node.set("condition", writeCondition(character.condition()));
        node.set("blessings", objectMapper.valueToTree(character.blessings()));
        node.set("inventory", writeInventory(character.inventory()));
        node.set("equipment", writeEquipment(character.equipment()));
        node.set("money", writeMoney(character.money()));
        node.set("spells", writeSpells(character.spells()));
        node.set("additionalInfo", objectMapper.valueToTree(character.additionalInfo()));
        return node;
    }

    private Character readCharacter(JsonNode node, Ownership ownership) {
        Inventory inventory = readInventory(required(node, "inventory"));
        Character character = new Character(UUID.randomUUID(), ownership, Instant.now(),
                text(node, "name"), optionalText(node, "image"), readInfo(required(node, "info")),
                readStats(required(node, "stats")), readSkills(required(node, "skills")),
                readCondition(required(node, "condition")), readBlessings(required(node, "blessings")),
                readAdditionalInfo(required(node, "additionalInfo")), inventory,
                readMoney(required(node, "money")), readSpells(required(node, "spells")));
        readEquipment(required(node, "equipment"), character);
        return character;
    }

    private ObjectNode writeStats(CharacterStats stats) {
        ObjectNode node = objectMapper.createObjectNode();
        for (StatGroup group : StatGroup.values()) {
            node.put(lowerCamel(group), stats.level(group));
        }
        return node;
    }

    private ObjectNode writeInfo(CharacterInfo info) {
        ObjectNode node = objectMapper.createObjectNode();
        node.put("level", info.level());
        node.put("origin", info.origin());
        node.put("background", info.background());
        node.put("class", info.className());
        node.put("specialization", info.specialization());
        return node;
    }

    private CharacterStats readStats(JsonNode node) {
        EnumMap<StatGroup, Integer> stats = new EnumMap<>(StatGroup.class);
        for (StatGroup group : StatGroup.values()) {
            stats.put(group, integer(node, lowerCamel(group)));
        }
        return new CharacterStats(stats);
    }

    private ArrayNode writeSkills(Map<SkillName, CharacterSkill> skills) {
        ArrayNode nodes = objectMapper.createArrayNode();
        skills.values().forEach(skill -> {
            ObjectNode node = nodes.addObject();
            node.put("stat", skill.statGroup().name());
            node.put("name", skill.name().name());
            node.put("level", skill.level());
        });
        return nodes;
    }

    private Map<SkillName, CharacterSkill> readSkills(JsonNode node) {
        Map<SkillName, CharacterSkill> skills = new LinkedHashMap<>();
        node.forEach(skillNode -> {
            SkillName name = enumValue(SkillName.class, skillNode, "name");
            StatGroup statedGroup = enumValue(StatGroup.class, skillNode, "stat");
            if (name.statGroup() != statedGroup || skills.containsKey(name)) {
                throw new IllegalArgumentException("invalid or duplicate skill " + name);
            }
            skills.put(name, new CharacterSkill(name, integer(skillNode, "level")));
        });
        if (skills.size() != SkillName.values().length) {
            throw new IllegalArgumentException("all predefined skills are required");
        }
        return skills;
    }

    private ObjectNode writeCondition(CharacterCondition condition) {
        ObjectNode node = objectMapper.createObjectNode();
        ObjectNode hp = node.putObject("hp");
        condition.hp().forEach((bodyPart, health) -> {
            ObjectNode bodyPartNode = hp.putObject(bodyPartJsonName(bodyPart));
            bodyPartNode.put("max", health.maxHp());
            bodyPartNode.put("current", health.currentHp());
        });
        node.put("passiveDefense", condition.passiveDefense());
        node.put("movementSpeed", condition.movementSpeed());
        node.put("maxCarryWeight", condition.maxCarryWeight());
        return node;
    }

    private CharacterCondition readCondition(JsonNode node) {
        JsonNode hpNode = required(node, "hp");
        EnumMap<BodyPart, BodyPartHealth> hp = new EnumMap<>(BodyPart.class);
        for (BodyPart bodyPart : BodyPart.values()) {
            JsonNode part = required(hpNode, bodyPartJsonName(bodyPart));
            hp.put(bodyPart, new BodyPartHealth(integer(part, "max"), integer(part, "current")));
        }
        return new CharacterCondition(hp, integer(node, "passiveDefense"),
                decimal(node, "movementSpeed"), decimal(node, "maxCarryWeight"));
    }

    private ObjectNode writeInventory(Inventory inventory) {
        ObjectNode node = objectMapper.createObjectNode();
        ArrayNode items = node.putArray("items");
        inventory.items().values().forEach(item -> items.add(writeItem(item)));
        ArrayNode slots = node.putArray("slots");
        inventory.slots().values().forEach(slot -> {
            ObjectNode slotNode = slots.addObject().put("index", slot.slotIndex());
            if (slot.itemId() == null) {
                slotNode.putNull("itemId");
            } else {
                slotNode.put("itemId", slot.itemId().toString());
            }
        });
        return node;
    }

    private ObjectNode writeItem(Item item) {
        ObjectNode node = objectMapper.createObjectNode();
        node.put("id", item.id().toString());
        node.put("type", item.type().name());
        node.put("title", item.title());
        node.put("image", item.image());
        node.put("weight", item.weight());
        node.put("description", item.description());
        if (item.equipmentType() != null) {
            node.put("equipmentType", item.equipmentType().name());
        }
        if (item.sellPriceBase() != null) {
            node.put("sellPriceBase", item.sellPriceBase());
        }
        return node;
    }

    private Inventory readInventory(JsonNode node) {
        Map<UUID, Item> items = new LinkedHashMap<>();
        required(node, "items").forEach(itemNode -> {
            Item item = readItem(itemNode);
            if (items.put(item.id(), item) != null) {
                throw new IllegalArgumentException("duplicate item id");
            }
        });
        Inventory inventory = new Inventory();
        required(node, "slots").forEach(slotNode -> {
            int slotIndex = integer(slotNode, "index");
            inventory.ensureSlot(slotIndex);
            JsonNode itemIdNode = slotNode.get("itemId");
            if (itemIdNode != null && !itemIdNode.isNull()) {
                UUID itemId = UUID.fromString(itemIdNode.asText());
                Item item = items.remove(itemId);
                if (item == null) {
                    throw new IllegalArgumentException("slot references unknown or duplicate item");
                }
                inventory.add(item, slotIndex);
            }
        });
        if (!items.isEmpty()) {
            throw new IllegalArgumentException("each inventory item must occupy a slot");
        }
        return inventory;
    }

    private Item readItem(JsonNode node) {
        ItemType type = enumValue(ItemType.class, node, "type");
        EquipmentType equipmentType = node.hasNonNull("equipmentType")
                ? enumValue(EquipmentType.class, node, "equipmentType") : null;
        BigDecimal sellPriceBase = node.hasNonNull("sellPriceBase")
                ? moneyDecimal(node, "sellPriceBase") : null;
        return new Item(UUID.fromString(text(node, "id")), type, text(node, "title"),
                text(node, "image"), decimal(node, "weight"), optionalText(node, "description"),
                equipmentType, sellPriceBase);
    }

    private ObjectNode writeEquipment(Map<EquipmentSlotCode, EquipmentSlot> equipment) {
        ObjectNode node = objectMapper.createObjectNode();
        equipment.forEach((code, slot) -> {
            if (slot.itemId() == null) {
                node.putNull(code.name());
            } else {
                node.put(code.name(), slot.itemId().toString());
            }
        });
        return node;
    }

    private void readEquipment(JsonNode node, Character character) {
        for (EquipmentSlotCode code : EquipmentSlotCode.values()) {
            JsonNode itemIdNode = node.get(code.name());
            if (itemIdNode != null && !itemIdNode.isNull()) {
                character.equip(code, UUID.fromString(itemIdNode.asText()));
            }
        }
    }

    private ObjectNode writeMoney(Money money) {
        ObjectNode node = objectMapper.createObjectNode();
        node.put("amountBase", money.amountBase());
        node.put("displayCurrency", money.displayCurrency().name());
        return node;
    }

    private Money readMoney(JsonNode node) {
        return new Money(moneyDecimal(node, "amountBase"),
                enumValue(Currency.class, node, "displayCurrency"));
    }

    private ArrayNode writeSpells(Map<UUID, Spell> spells) {
        ArrayNode nodes = objectMapper.createArrayNode();
        spells.values().forEach(spell -> {
            ObjectNode node = nodes.addObject();
            node.put("id", spell.id().toString());
            node.put("name", spell.name());
            node.put("type", spell.type().name());
            node.put("class", spell.spellClass().name());
            node.put("image", spell.image());
            node.put("requirements", spell.requirements());
            node.put("description", spell.description());
        });
        return nodes;
    }

    private Map<UUID, Spell> readSpells(JsonNode node) {
        Map<UUID, Spell> spells = new LinkedHashMap<>();
        node.forEach(spellNode -> {
            UUID id = UUID.fromString(text(spellNode, "id"));
            Spell spell = new Spell(id, text(spellNode, "name"),
                    enumValue(SpellType.class, spellNode, "type"),
                    enumValue(SpellClass.class, spellNode, "class"), text(spellNode, "image"),
                    text(spellNode, "requirements"), optionalText(spellNode, "description"));
            if (spells.put(id, spell) != null) {
                throw new IllegalArgumentException("duplicate spell id");
            }
        });
        return spells;
    }

    private static CharacterInfo readInfo(JsonNode node) {
        return new CharacterInfo(integer(node, "level"), optionalText(node, "origin"),
                optionalText(node, "background"), optionalText(node, "class"),
                optionalText(node, "specialization"));
    }

    private static BlessingInspiration readBlessings(JsonNode node) {
        return new BlessingInspiration(integer(node, "blessings"), integer(node, "inspirations"));
    }

    private static AdditionalInfo readAdditionalInfo(JsonNode node) {
        return new AdditionalInfo(optionalText(node, "appearance"), optionalText(node, "detailedOrigin"),
                optionalText(node, "allies"), optionalText(node, "notesPrimary"),
                optionalText(node, "notesSecondary"));
    }

    private static JsonNode required(JsonNode node, String field) {
        JsonNode value = node.get(field);
        if (value == null || value.isNull()) {
            throw new IllegalArgumentException(field + " is required");
        }
        return value;
    }

    private static String text(JsonNode node, String field) {
        String value = required(node, field).asText();
        if (value.isBlank()) {
            throw new IllegalArgumentException(field + " must not be blank");
        }
        return value;
    }

    private static String optionalText(JsonNode node, String field) {
        JsonNode value = node.get(field);
        return value == null || value.isNull() ? "" : value.asText();
    }

    private static void requireText(JsonNode node, String field, String expected) {
        String actual = text(node, field);
        if (!expected.equals(actual)) {
            throw new IllegalArgumentException("unsupported " + field + ": " + actual);
        }
    }

    private static int integer(JsonNode node, String field) {
        JsonNode fieldNode = required(node, field);
        if (!fieldNode.isIntegralNumber()) {
            throw new IllegalArgumentException(field + " must be an integer");
        }
        int value = fieldNode.intValue();
        if (value < 0) {
            throw new IllegalArgumentException(field + " must be non-negative");
        }
        return value;
    }

    private static BigDecimal decimal(JsonNode node, String field) {
        JsonNode fieldNode = required(node, field);
        if (!fieldNode.isNumber()) {
            throw new IllegalArgumentException(field + " must be a number");
        }
        BigDecimal value = fieldNode.decimalValue();
        if (value.signum() < 0) {
            throw new IllegalArgumentException(field + " must be non-negative");
        }
        return value;
    }

    private static BigDecimal moneyDecimal(JsonNode node, String field) {
        BigDecimal value = decimal(node, field);
        if (value.scale() > 2) {
            throw new IllegalArgumentException(field + " must have at most two decimal places");
        }
        return value;
    }

    private static <T extends Enum<T>> T enumValue(Class<T> enumType, JsonNode node, String field) {
        try {
            return Enum.valueOf(enumType, text(node, field));
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException("invalid " + field, exception);
        }
    }

    private static String lowerCamel(StatGroup statGroup) {
        String name = statGroup.name().toLowerCase();
        return name;
    }

    private static String bodyPartJsonName(BodyPart bodyPart) {
        return switch (bodyPart) {
            case HEAD -> "head";
            case NECK -> "neck";
            case TORSO -> "torso";
            case LEFT_ARM -> "leftArm";
            case RIGHT_ARM -> "rightArm";
            case LEFT_LEG -> "leftLeg";
            case RIGHT_LEG -> "rightLeg";
        };
    }
}
