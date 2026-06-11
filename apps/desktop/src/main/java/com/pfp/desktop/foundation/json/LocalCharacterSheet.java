package com.pfp.desktop.foundation.json;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public record LocalCharacterSheet(
        String id,
        String name,
        String image,
        Info info,
        Stats stats,
        List<Skill> skills,
        Condition condition,
        Blessings blessings,
        Money money,
        Inventory inventory,
        List<EquipmentSlot> equipment,
        List<SpellPreview> spells,
        AdditionalInfo additionalInfo
) {
    private static final List<String> EQUIPMENT_CODES = List.of(
            "HEAD", "NECK", "TORSO", "ARMS", "LEGS",
            "WEAPON_1", "WEAPON_2",
            "TALISMAN_1", "TALISMAN_2", "TALISMAN_3", "TALISMAN_4"
    );

    public LocalCharacterSheet {
        name = text(name);
        image = text(image);
        skills = skills == null ? List.of() : List.copyOf(skills);
        condition = condition == null ? new Condition(BodyHealth.defaults(), 0, BigDecimal.ZERO, BigDecimal.ZERO) : condition;
        blessings = blessings == null ? new Blessings(0, 0) : blessings;
        money = money == null ? new Money(BigDecimal.ZERO, "CURRENCY_1") : money;
        inventory = inventory == null ? Inventory.empty() : inventory;
        equipment = equipment == null ? List.of() : List.copyOf(equipment);
        spells = spells == null ? List.of() : List.copyOf(spells);
        additionalInfo = additionalInfo == null ? new AdditionalInfo("", "", "", "", "") : additionalInfo;
    }

    static LocalCharacterSheet fromJson(String id, JsonNode root) {
        JsonNode character = root.path("character");
        JsonNode info = character.path("info");
        JsonNode stats = character.path("stats");
        JsonNode condition = character.path("condition");
        JsonNode blessings = character.path("blessings");
        JsonNode money = character.path("money");
        JsonNode inventory = character.path("inventory");
        JsonNode equipment = character.path("equipment");
        JsonNode additionalInfo = character.path("additionalInfo");
        return new LocalCharacterSheet(
                id,
                textOrDefault(character, "name", "New Character"),
                textOrDefault(character, "image", ""),
                new Info(
                        intOrDefault(info, "level", 1),
                        textOrDefault(info, "origin", ""),
                        textOrDefault(info, "background", ""),
                        textOrDefault(info, "class", ""),
                        textOrDefault(info, "specialization", "")
                ),
                new Stats(
                        intOrDefault(stats, "strength", 0),
                        intOrDefault(stats, "dexterity", 0),
                        intOrDefault(stats, "stamina", 0),
                        intOrDefault(stats, "intelligence", 0),
                        intOrDefault(stats, "charisma", 0),
                        intOrDefault(stats, "luck", 0),
                        intOrDefault(stats, "mind", 0)
                ),
                readSkills(character.path("skills")),
                new Condition(
                        readBodyHealth(condition.path("hp")),
                        intOrDefault(condition, "passiveDefense", 0),
                        decimalOrDefault(condition, "movementSpeed"),
                        decimalOrDefault(condition, "maxCarryWeight")
                ),
                new Blessings(
                        intOrDefault(blessings, "blessings", 0),
                        intOrDefault(blessings, "inspirations", 0)
                ),
                new Money(
                        decimalOrDefault(money, "amountBase"),
                        textOrDefault(money, "displayCurrency", "CURRENCY_1")
                ),
                readInventory(inventory),
                readEquipment(equipment),
                readSpells(character.path("spells")),
                new AdditionalInfo(
                        textOrDefault(additionalInfo, "appearance", ""),
                        textOrDefault(additionalInfo, "detailedOrigin", ""),
                        textOrDefault(additionalInfo, "allies", ""),
                        textOrDefault(additionalInfo, "notesPrimary", ""),
                        textOrDefault(additionalInfo, "notesSecondary", "")
                )
        );
    }

    void applyTo(ObjectNode root) {
        ObjectNode character = (ObjectNode) root.path("character");
        character.put("name", name.isBlank() ? "New Character" : name);
        character.put("image", image);

        ObjectNode infoNode = (ObjectNode) character.path("info");
        infoNode.put("level", Math.max(1, info.level()));
        infoNode.put("origin", info.origin());
        infoNode.put("background", info.background());
        infoNode.put("class", info.className());
        infoNode.put("specialization", info.specialization());

        ObjectNode statsNode = (ObjectNode) character.path("stats");
        statsNode.put("strength", nonNegative(stats.strength()));
        statsNode.put("dexterity", nonNegative(stats.dexterity()));
        statsNode.put("stamina", nonNegative(stats.stamina()));
        statsNode.put("intelligence", nonNegative(stats.intelligence()));
        statsNode.put("charisma", nonNegative(stats.charisma()));
        statsNode.put("luck", nonNegative(stats.luck()));
        statsNode.put("mind", nonNegative(stats.mind()));

        ArrayNode skillsNode = (ArrayNode) character.path("skills");
        for (Skill skill : skills) {
            updateSkill(skillsNode, skill);
        }

        ObjectNode conditionNode = (ObjectNode) character.path("condition");
        ObjectNode hpNode = (ObjectNode) conditionNode.path("hp");
        updateBodyPart(hpNode, "head", condition.hp().head());
        updateBodyPart(hpNode, "neck", condition.hp().neck());
        updateBodyPart(hpNode, "torso", condition.hp().torso());
        updateBodyPart(hpNode, "leftArm", condition.hp().leftArm());
        updateBodyPart(hpNode, "rightArm", condition.hp().rightArm());
        updateBodyPart(hpNode, "leftLeg", condition.hp().leftLeg());
        updateBodyPart(hpNode, "rightLeg", condition.hp().rightLeg());
        conditionNode.put("passiveDefense", nonNegative(condition.passiveDefense()));
        conditionNode.put("movementSpeed", nonNegative(condition.movementSpeed()));
        conditionNode.put("maxCarryWeight", nonNegative(condition.maxCarryWeight()));

        ObjectNode blessingsNode = (ObjectNode) character.path("blessings");
        blessingsNode.put("blessings", nonNegative(blessings.blessings()));
        blessingsNode.put("inspirations", nonNegative(blessings.inspirations()));

        ObjectNode moneyNode = (ObjectNode) character.path("money");
        moneyNode.put("amountBase", nonNegative(money.amountBase()));
        moneyNode.put("displayCurrency", money.displayCurrency().isBlank() ? "CURRENCY_1" : money.displayCurrency());

        ObjectNode inventoryNode = (ObjectNode) character.path("inventory");
        ArrayNode itemsNode = inventoryNode.putArray("items");
        for (InventoryItem item : inventory.items()) {
            ObjectNode itemNode = itemsNode.addObject();
            itemNode.put("id", item.id());
            itemNode.put("type", item.type());
            itemNode.put("title", item.title());
            itemNode.put("image", item.image());
            itemNode.put("weight", nonNegative(item.weight()));
            itemNode.put("description", item.description());
            if ("EQUIPMENT".equals(item.type())) {
                itemNode.put("equipmentType", item.equipmentType().isBlank() ? "HEAD" : item.equipmentType());
            }
            if ("TRADE".equals(item.type())) {
                itemNode.put("sellPriceBase", nonNegative(item.sellPriceBase()));
            }
        }
        ArrayNode slotsNode = inventoryNode.putArray("slots");
        for (InventorySlot slot : inventory.slots()) {
            ObjectNode slotNode = slotsNode.addObject();
            slotNode.put("index", slot.index());
            if (slot.itemId().isBlank()) {
                slotNode.putNull("itemId");
            } else {
                slotNode.put("itemId", slot.itemId());
            }
        }

        ObjectNode equipmentNode = (ObjectNode) character.path("equipment");
        for (String code : EQUIPMENT_CODES) {
            String itemId = equipment.stream()
                    .filter(slot -> slot.code().equals(code))
                    .map(EquipmentSlot::itemId)
                    .findFirst()
                    .orElse("");
            if (itemId.isBlank()) {
                equipmentNode.putNull(code);
            } else {
                equipmentNode.put(code, itemId);
            }
        }

        ArrayNode spellsNode = character.putArray("spells");
        for (SpellPreview spell : spells) {
            ObjectNode spellNode = spellsNode.addObject();
            spellNode.put("id", spell.id());
            spellNode.put("name", spell.name());
            spellNode.put("type", spell.type());
            spellNode.put("class", spell.spellClass());
            spellNode.put("requirements", spell.requirements());
            spellNode.put("image", spell.image());
            spellNode.put("description", spell.description());
        }

        ObjectNode additionalNode = (ObjectNode) character.path("additionalInfo");
        additionalNode.put("appearance", additionalInfo.appearance());
        additionalNode.put("detailedOrigin", additionalInfo.detailedOrigin());
        additionalNode.put("allies", additionalInfo.allies());
        additionalNode.put("notesPrimary", additionalInfo.notesPrimary());
        additionalNode.put("notesSecondary", additionalInfo.notesSecondary());
    }

    LocalCharacterRecord toRecord(LocalCharacterRecord existing, String updatedAt) {
        return new LocalCharacterRecord(
                existing.id(),
                name.isBlank() ? "New Character" : name,
                image,
                Math.max(1, info.level()),
                info.className(),
                info.specialization(),
                existing.createdAt(),
                updatedAt == null || updatedAt.isBlank() ? Instant.now().toString() : updatedAt,
                existing.fileName()
        );
    }

    private static String text(String value) {
        return value == null ? "" : value;
    }

    private static int nonNegative(int value) {
        return Math.max(0, value);
    }

    private static BigDecimal nonNegative(BigDecimal value) {
        if (value == null || value.signum() < 0) {
            return BigDecimal.ZERO;
        }
        return value;
    }

    private static String supportedCurrency(String value) {
        String currency = text(value);
        return switch (currency) {
            case "CURRENCY_1", "CURRENCY_2", "CURRENCY_3", "CURRENCY_4" -> currency;
            default -> "CURRENCY_1";
        };
    }

    private static String textOrDefault(JsonNode node, String field, String defaultValue) {
        JsonNode value = node.get(field);
        if (value == null || value.isNull()) {
            return defaultValue;
        }
        return value.asText(defaultValue);
    }

    private static int intOrDefault(JsonNode node, String field, int defaultValue) {
        JsonNode value = node.get(field);
        if (value == null || !value.isIntegralNumber()) {
            return defaultValue;
        }
        return value.intValue();
    }

    private static BigDecimal decimalOrDefault(JsonNode node, String field) {
        JsonNode value = node.get(field);
        if (value == null || !value.isNumber()) {
            return BigDecimal.ZERO;
        }
        return value.decimalValue();
    }

    private static List<Skill> readSkills(JsonNode node) {
        List<Skill> skills = new ArrayList<>();
        if (node != null && node.isArray()) {
            for (JsonNode skill : node) {
                skills.add(new Skill(
                        textOrDefault(skill, "stat", ""),
                        textOrDefault(skill, "name", ""),
                        intOrDefault(skill, "level", 0)
                ));
            }
        }
        return List.copyOf(skills);
    }

    private static BodyHealth readBodyHealth(JsonNode hp) {
        return new BodyHealth(
                readBodyPart(hp, "head", 60),
                readBodyPart(hp, "neck", 40),
                readBodyPart(hp, "torso", 100),
                readBodyPart(hp, "leftArm", 60),
                readBodyPart(hp, "rightArm", 60),
                readBodyPart(hp, "leftLeg", 60),
                readBodyPart(hp, "rightLeg", 60)
        );
    }

    private static BodyPartHealth readBodyPart(JsonNode hp, String field, int defaultMax) {
        JsonNode part = hp.path(field);
        int max = intOrDefault(part, "max", defaultMax);
        if (max == 0) {
            return new BodyPartHealth(defaultMax, defaultMax);
        }
        return new BodyPartHealth(
                intOrDefault(part, "current", max),
                max
        );
    }

    private static Inventory readInventory(JsonNode inventory) {
        List<InventoryItem> items = new ArrayList<>();
        JsonNode itemNodes = inventory.path("items");
        if (itemNodes.isArray()) {
            for (JsonNode item : itemNodes) {
                String type = textOrDefault(item, "type", "ITEM");
                items.add(new InventoryItem(
                        textOrDefault(item, "id", ""),
                        supportedItemType(type),
                        textOrDefault(item, "title", "Untitled item"),
                        textOrDefault(item, "image", ""),
                        decimalOrDefault(item, "weight"),
                        textOrDefault(item, "description", ""),
                        textOrDefault(item, "equipmentType", "EQUIPMENT".equals(type) ? "HEAD" : ""),
                        decimalOrDefault(item, "sellPriceBase")
                ));
            }
        }
        List<InventorySlot> slots = new ArrayList<>();
        JsonNode slotNodes = inventory.path("slots");
        if (slotNodes.isArray()) {
            for (JsonNode slot : slotNodes) {
                slots.add(new InventorySlot(
                        intOrDefault(slot, "index", slots.size()),
                        slot.path("itemId").isNull() ? "" : textOrDefault(slot, "itemId", "")
                ));
            }
        }
        if (slots.isEmpty()) {
            for (int index = 0; index < 10; index++) {
                slots.add(new InventorySlot(index, ""));
            }
        }
        return new Inventory(items, slots);
    }

    private static List<EquipmentSlot> readEquipment(JsonNode equipment) {
        List<EquipmentSlot> slots = new ArrayList<>();
        if (equipment != null && equipment.isObject()) {
            Iterator<String> fields = equipment.fieldNames();
            while (fields.hasNext()) {
                String code = fields.next();
                JsonNode value = equipment.path(code);
                slots.add(new EquipmentSlot(code, value.isNull() ? "" : value.asText("")));
            }
        }
        return List.copyOf(slots);
    }

    private static String supportedItemType(String value) {
        return switch (text(value)) {
            case "EQUIPMENT" -> "EQUIPMENT";
            case "TRADE" -> "TRADE";
            default -> "ITEM";
        };
    }

    private static List<SpellPreview> readSpells(JsonNode node) {
        List<SpellPreview> spells = new ArrayList<>();
        if (node != null && node.isArray()) {
            for (JsonNode spell : node) {
                spells.add(new SpellPreview(
                        textOrDefault(spell, "id", ""),
                        textOrDefault(spell, "name", ""),
                        textOrDefault(spell, "type", ""),
                        textOrDefault(spell, "class", ""),
                        textOrDefault(spell, "requirements", ""),
                        textOrDefault(spell, "image", ""),
                        textOrDefault(spell, "description", "")
                ));
            }
        }
        return List.copyOf(spells);
    }

    private static void updateSkill(ArrayNode skillsNode, Skill skill) {
        if (skill.name().isBlank()) {
            return;
        }
        for (JsonNode node : skillsNode) {
            if (skill.name().equals(node.path("name").asText())) {
                ((ObjectNode) node).put("level", nonNegative(skill.level()));
                return;
            }
        }
        ObjectNode newSkill = skillsNode.addObject();
        newSkill.put("stat", skill.stat());
        newSkill.put("name", skill.name());
        newSkill.put("level", nonNegative(skill.level()));
    }

    private static void updateBodyPart(ObjectNode hpNode, String field, BodyPartHealth part) {
        ObjectNode node = (ObjectNode) hpNode.path(field);
        int max = nonNegative(part.max());
        node.put("max", max);
        node.put("current", Math.min(nonNegative(part.current()), max));
    }

    public record Info(int level, String origin, String background, String className, String specialization) {
        public Info {
            level = Math.max(1, level);
            origin = text(origin);
            background = text(background);
            className = text(className);
            specialization = text(specialization);
        }
    }

    public record Stats(int strength, int dexterity, int stamina, int intelligence, int charisma, int luck, int mind) {
        public Stats {
            strength = nonNegative(strength);
            dexterity = nonNegative(dexterity);
            stamina = nonNegative(stamina);
            intelligence = nonNegative(intelligence);
            charisma = nonNegative(charisma);
            luck = nonNegative(luck);
            mind = nonNegative(mind);
        }
    }

    public record Skill(String stat, String name, int level) {
        public Skill {
            stat = text(stat);
            name = text(name);
            level = nonNegative(level);
        }
    }

    public record Condition(BodyHealth hp, int passiveDefense, BigDecimal movementSpeed, BigDecimal maxCarryWeight) {
        public Condition {
            hp = hp == null ? BodyHealth.defaults() : hp;
            passiveDefense = nonNegative(passiveDefense);
            movementSpeed = nonNegative(movementSpeed);
            maxCarryWeight = nonNegative(maxCarryWeight);
        }
    }

    public record BodyHealth(
            BodyPartHealth head,
            BodyPartHealth neck,
            BodyPartHealth torso,
            BodyPartHealth leftArm,
            BodyPartHealth rightArm,
            BodyPartHealth leftLeg,
            BodyPartHealth rightLeg
    ) {
        private static BodyHealth defaults() {
            return new BodyHealth(
                    new BodyPartHealth(60, 60),
                    new BodyPartHealth(40, 40),
                    new BodyPartHealth(100, 100),
                    new BodyPartHealth(60, 60),
                    new BodyPartHealth(60, 60),
                    new BodyPartHealth(60, 60),
                    new BodyPartHealth(60, 60)
            );
        }
    }

    public record BodyPartHealth(int current, int max) {
        public BodyPartHealth {
            max = nonNegative(max);
            current = Math.min(nonNegative(current), max);
        }
    }

    public record Blessings(int blessings, int inspirations) {
        public Blessings {
            blessings = nonNegative(blessings);
            inspirations = nonNegative(inspirations);
        }
    }

    public record Money(BigDecimal amountBase, String displayCurrency) {
        public Money {
            amountBase = nonNegative(amountBase);
            displayCurrency = supportedCurrency(displayCurrency);
        }
    }

    public record Inventory(List<InventoryItem> items, List<InventorySlot> slots) {
        public Inventory {
            items = items == null ? List.of() : List.copyOf(items);
            slots = slots == null ? List.of() : List.copyOf(slots);
        }

        private static Inventory empty() {
            List<InventorySlot> slots = new ArrayList<>();
            for (int index = 0; index < 10; index++) {
                slots.add(new InventorySlot(index, ""));
            }
            return new Inventory(List.of(), slots);
        }

        public int slotCount() {
            return slots.size();
        }

        public int occupiedSlots() {
            int count = 0;
            for (InventorySlot slot : slots) {
                if (!slot.itemId().isBlank()) {
                    count++;
                }
            }
            return count;
        }

        public int itemCount() {
            return items.size();
        }

        public BigDecimal currentWeight() {
            BigDecimal weight = BigDecimal.ZERO;
            for (InventoryItem item : items) {
                weight = weight.add(item.weight());
            }
            return weight;
        }
    }

    public record InventoryItem(
            String id,
            String type,
            String title,
            String image,
            BigDecimal weight,
            String description,
            String equipmentType,
            BigDecimal sellPriceBase
    ) {
        public InventoryItem {
            id = text(id);
            type = supportedItemType(type);
            title = text(title).isBlank() ? "Untitled item" : text(title);
            image = text(image);
            weight = nonNegative(weight);
            description = text(description);
            equipmentType = "EQUIPMENT".equals(type) ? text(equipmentType) : "";
            if ("EQUIPMENT".equals(type) && equipmentType.isBlank()) {
                equipmentType = "HEAD";
            }
            sellPriceBase = "TRADE".equals(type) ? nonNegative(sellPriceBase) : BigDecimal.ZERO;
        }

        public InventoryItem withTitle(String title) {
            return new InventoryItem(id, type, title, image, weight, description, equipmentType, sellPriceBase);
        }

        public InventoryItem withImage(String image) {
            return new InventoryItem(id, type, title, image, weight, description, equipmentType, sellPriceBase);
        }

        public InventoryItem withDescription(String description) {
            return new InventoryItem(id, type, title, image, weight, description, equipmentType, sellPriceBase);
        }

        public InventoryItem withWeight(BigDecimal weight) {
            return new InventoryItem(id, type, title, image, weight, description, equipmentType, sellPriceBase);
        }

        public InventoryItem withSellPrice(BigDecimal sellPriceBase) {
            return new InventoryItem(id, type, title, image, weight, description, equipmentType, sellPriceBase);
        }
    }

    public record InventorySlot(int index, String itemId) {
        public InventorySlot {
            index = nonNegative(index);
            itemId = text(itemId);
        }
    }

    public record EquipmentSlot(String code, String itemId) {
        public EquipmentSlot {
            code = text(code);
            itemId = text(itemId);
        }
    }

    public record SpellPreview(String id, String name, String type, String spellClass, String requirements, String image, String description) {
        public SpellPreview {
            id = text(id);
            name = text(name);
            type = text(type);
            spellClass = text(spellClass);
            requirements = text(requirements);
            image = text(image);
            description = text(description);
        }

        public SpellPreview withName(String name) {
            return new SpellPreview(id, name, type, spellClass, requirements, image, description);
        }

        public SpellPreview withType(String type) {
            return new SpellPreview(id, name, type, spellClass, requirements, image, description);
        }

        public SpellPreview withSpellClass(String spellClass) {
            return new SpellPreview(id, name, type, spellClass, requirements, image, description);
        }

        public SpellPreview withRequirements(String requirements) {
            return new SpellPreview(id, name, type, spellClass, requirements, image, description);
        }

        public SpellPreview withImage(String image) {
            return new SpellPreview(id, name, type, spellClass, requirements, image, description);
        }

        public SpellPreview withDescription(String description) {
            return new SpellPreview(id, name, type, spellClass, requirements, image, description);
        }
    }

    public record AdditionalInfo(
            String appearance,
            String detailedOrigin,
            String allies,
            String notesPrimary,
            String notesSecondary
    ) {
        public AdditionalInfo {
            appearance = text(appearance);
            detailedOrigin = text(detailedOrigin);
            allies = text(allies);
            notesPrimary = text(notesPrimary);
            notesSecondary = text(notesSecondary);
        }
    }
}
