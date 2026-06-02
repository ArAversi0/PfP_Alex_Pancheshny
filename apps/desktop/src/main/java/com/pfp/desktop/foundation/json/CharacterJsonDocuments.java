package com.pfp.desktop.foundation.json;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.math.BigDecimal;

final class CharacterJsonDocuments {

    static final String SCHEMA_VERSION = "1.0";

    private static final String[][] SKILLS = {
            {"ATHLETICS", "STRENGTH"},
            {"BLOCKING", "STRENGTH"},
            {"GRAPPLING", "STRENGTH"},
            {"BRUTE_FORCE", "STRENGTH"},
            {"REFLEXES", "DEXTERITY"},
            {"EVASION", "DEXTERITY"},
            {"ACROBATICS", "DEXTERITY"},
            {"STEALTH", "DEXTERITY"},
            {"SLEIGHT_OF_HAND", "DEXTERITY"},
            {"BALANCE", "DEXTERITY"},
            {"PAIN_TOLERANCE", "STAMINA"},
            {"ENDURANCE", "STAMINA"},
            {"CARRYING_CAPACITY", "STAMINA"},
            {"ANALYSIS", "INTELLIGENCE"},
            {"MAGIC", "INTELLIGENCE"},
            {"RELIGION", "INTELLIGENCE"},
            {"SURVIVAL", "INTELLIGENCE"},
            {"MEDICINE", "INTELLIGENCE"},
            {"SCIENCE", "INTELLIGENCE"},
            {"AWARENESS", "INTELLIGENCE"},
            {"RHETORIC", "CHARISMA"},
            {"PERFORMANCE", "CHARISMA"},
            {"INTIMIDATION", "CHARISMA"},
            {"CHARM", "CHARISMA"},
            {"BUSINESS_SENSE", "CHARISMA"},
            {"FORTUNE", "LUCK"},
            {"PROFIT", "LUCK"},
            {"WILLPOWER", "MIND"},
            {"COMPOSURE", "MIND"},
            {"SUGGESTION", "MIND"}
    };

    private CharacterJsonDocuments() {
    }

    static ObjectNode newCharacter(ObjectMapper objectMapper, String name) {
        ObjectNode root = objectMapper.createObjectNode();
        root.put("schemaVersion", SCHEMA_VERSION);

        ObjectNode character = root.putObject("character");
        character.put("name", name);
        character.put("image", "");
        character.set("info", info(character));
        character.set("stats", stats(character));
        character.set("skills", skills(character));
        character.set("condition", condition(character));
        character.set("blessings", blessings(character));
        character.set("inventory", inventory(character));
        character.set("equipment", equipment(character));
        character.set("money", money(character));
        character.set("spells", character.arrayNode());
        character.set("additionalInfo", additionalInfo(character));
        return root;
    }

    private static ObjectNode info(ObjectNode parent) {
        ObjectNode node = parent.objectNode();
        node.put("level", 1);
        node.put("origin", "");
        node.put("background", "");
        node.put("class", "");
        node.put("specialization", "");
        return node;
    }

    private static ObjectNode stats(ObjectNode parent) {
        ObjectNode node = parent.objectNode();
        node.put("strength", 0);
        node.put("dexterity", 0);
        node.put("stamina", 0);
        node.put("intelligence", 0);
        node.put("charisma", 0);
        node.put("luck", 0);
        node.put("mind", 0);
        return node;
    }

    private static ArrayNode skills(ObjectNode parent) {
        ArrayNode nodes = parent.arrayNode();
        for (String[] skill : SKILLS) {
            ObjectNode node = nodes.addObject();
            node.put("stat", skill[1]);
            node.put("name", skill[0]);
            node.put("level", 0);
        }
        return nodes;
    }

    private static ObjectNode condition(ObjectNode parent) {
        ObjectNode node = parent.objectNode();
        ObjectNode hp = node.putObject("hp");
        addBodyPart(hp, "head", 60);
        addBodyPart(hp, "neck", 40);
        addBodyPart(hp, "torso", 100);
        addBodyPart(hp, "leftArm", 60);
        addBodyPart(hp, "rightArm", 60);
        addBodyPart(hp, "leftLeg", 60);
        addBodyPart(hp, "rightLeg", 60);
        node.put("passiveDefense", 0);
        node.put("movementSpeed", BigDecimal.ZERO);
        node.put("maxCarryWeight", BigDecimal.ZERO);
        return node;
    }

    private static void addBodyPart(ObjectNode hp, String name, int maxHealth) {
        ObjectNode part = hp.putObject(name);
        part.put("max", maxHealth);
        part.put("current", maxHealth);
    }

    private static ObjectNode blessings(ObjectNode parent) {
        ObjectNode node = parent.objectNode();
        node.put("blessings", 0);
        node.put("inspirations", 0);
        return node;
    }

    private static ObjectNode inventory(ObjectNode parent) {
        ObjectNode node = parent.objectNode();
        node.set("items", parent.arrayNode());
        ArrayNode slots = parent.arrayNode();
        for (int index = 0; index < 10; index++) {
            ObjectNode slot = slots.addObject();
            slot.put("index", index);
            slot.putNull("itemId");
        }
        node.set("slots", slots);
        return node;
    }

    private static ObjectNode equipment(ObjectNode parent) {
        ObjectNode node = parent.objectNode();
        node.putNull("HEAD");
        node.putNull("NECK");
        node.putNull("TORSO");
        node.putNull("ARMS");
        node.putNull("LEGS");
        node.putNull("WEAPON_1");
        node.putNull("WEAPON_2");
        node.putNull("TALISMAN_1");
        node.putNull("TALISMAN_2");
        node.putNull("TALISMAN_3");
        node.putNull("TALISMAN_4");
        return node;
    }

    private static ObjectNode money(ObjectNode parent) {
        ObjectNode node = parent.objectNode();
        node.put("amountBase", BigDecimal.ZERO);
        node.put("displayCurrency", "CURRENCY_1");
        return node;
    }

    private static ObjectNode additionalInfo(ObjectNode parent) {
        ObjectNode node = parent.objectNode();
        node.put("appearance", "");
        node.put("detailedOrigin", "");
        node.put("allies", "");
        node.put("notesPrimary", "");
        node.put("notesSecondary", "");
        return node;
    }
}
