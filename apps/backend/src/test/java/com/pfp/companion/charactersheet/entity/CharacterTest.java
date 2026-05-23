package com.pfp.companion.charactersheet.entity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class CharacterTest {

    @Test
    void createsCharacterWithAgreedDefaults() {
        Character character = Character.createNew("Arden", Ownership.localGuest());

        assertThat(character.skills()).hasSize(SkillName.values().length);
        assertThat(character.stats().level(StatGroup.DEXTERITY)).isZero();
        assertThat(character.money().amountBase()).isEqualByComparingTo("0.00");
        assertThat(character.currentCarryWeight()).isEqualByComparingTo("0");
        assertThat(character.inventory().slots()).hasSize(10);
        assertThat(character.condition().healthResult().globalHealthPercent())
                .isEqualByComparingTo("100.00");
    }

    @Test
    void equipsCompatibleInventoryItemAndUnequipsItWhenThrownAway() {
        Character character = Character.createNew("Arden", Ownership.localGuest());
        Item helmet = equipment(EquipmentType.HEAD);
        character.inventory().add(helmet, 0);

        character.equip(EquipmentSlotCode.HEAD, helmet.id());
        character.throwAwayItem(helmet.id());

        assertThat(character.equipment().get(EquipmentSlotCode.HEAD).itemId()).isNull();
        assertThat(character.inventory().items()).doesNotContainKey(helmet.id());
    }

    @Test
    void rejectsIncompatibleEquipmentSlot() {
        Character character = Character.createNew("Arden", Ownership.localGuest());
        Item helmet = equipment(EquipmentType.HEAD);
        character.inventory().add(helmet, 0);

        assertThatThrownBy(() -> character.equip(EquipmentSlotCode.WEAPON_1, helmet.id()))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void sellsOnlyTradeItemsAndAddsBaseMoney() {
        Character character = Character.createNew("Arden", Ownership.localGuest());
        Item tradeItem = new Item(UUID.randomUUID(), ItemType.TRADE, "Gem", "gem.jpg",
                new BigDecimal("0.25"), "", null, new BigDecimal("12.50"));
        character.inventory().add(tradeItem, 0);

        character.sellTradeItem(tradeItem.id());

        assertThat(character.money().amountBase()).isEqualByComparingTo("12.50");
        assertThat(character.inventory().items()).doesNotContainKey(tradeItem.id());
    }

    @Test
    void addsItemToNearestFreeInventorySlot() {
        Character character = Character.createNew("Arden", Ownership.localGuest());
        character.inventory().ensureSlot(0);
        character.inventory().ensureSlot(1);
        character.inventory().add(new Item(UUID.randomUUID(), ItemType.ITEM, "Rope", "rope.jpg",
                BigDecimal.ONE, "", null, null), 0);
        Item torch = new Item(UUID.randomUUID(), ItemType.ITEM, "Torch", "torch.jpg",
                BigDecimal.ONE, "", null, null);

        int slotIndex = character.addItem(torch);

        assertThat(slotIndex).isEqualTo(1);
        assertThat(character.inventory().slots().get(1).itemId()).isEqualTo(torch.id());
    }

    @Test
    void expandsInventoryGridInRowsOfTenSlots() {
        Character character = Character.createNew("Arden", Ownership.localGuest());

        character.addInventoryRows(1);
        character.addInventoryRows(1);

        assertThat(character.inventory().slots()).hasSize(30);
        assertThat(character.inventory().slots()).containsKeys(0, 9, 10, 19, 20, 29);
    }

    @Test
    void rejectsUpdateThatMakesEquippedItemIncompatible() {
        Character character = Character.createNew("Arden", Ownership.localGuest());
        Item helmet = equipment(EquipmentType.HEAD);
        character.inventory().add(helmet, 0);
        character.equip(EquipmentSlotCode.HEAD, helmet.id());
        Item weapon = new Item(helmet.id(), ItemType.EQUIPMENT, "Sword", "sword.jpg",
                BigDecimal.ONE, "", EquipmentType.WEAPON, null);

        assertThatThrownBy(() -> character.updateItem(weapon))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("incompatible");
    }

    @Test
    void throwingAwayTradeItemDoesNotSellIt() {
        Character character = Character.createNew("Arden", Ownership.localGuest());
        Item tradeItem = new Item(UUID.randomUUID(), ItemType.TRADE, "Gem", "gem.jpg",
                new BigDecimal("0.25"), "", null, new BigDecimal("12.50"));
        character.inventory().add(tradeItem, 0);

        character.throwAwayItem(tradeItem.id());

        assertThat(character.money().amountBase()).isEqualByComparingTo("0.00");
    }

    @Test
    void managesSpellCardsAndDisplayCurrency() {
        Character character = Character.createNew("Arden", Ownership.localGuest());
        Spell spell = new Spell(UUID.randomUUID(), "Flame", SpellType.SPELL, SpellClass.PRIEST,
                "flame.jpg", "Level 2", "");
        character.addSpell(spell);
        character.selectDisplayCurrency(Currency.CURRENCY_3);

        Spell updated = new Spell(spell.id(), "Blessed Flame", SpellType.SPELL, SpellClass.PRIEST,
                "flame.jpg", "Level 3", "");
        character.updateSpell(updated);

        assertThat(character.spells()).containsEntry(spell.id(), updated);
        assertThat(character.money().displayCurrency()).isEqualTo(Currency.CURRENCY_3);

        character.deleteSpell(spell.id());

        assertThat(character.spells()).isEmpty();
    }

    private static Item equipment(EquipmentType equipmentType) {
        return new Item(UUID.randomUUID(), ItemType.EQUIPMENT, "Equipment", "equipment.jpg",
                BigDecimal.ONE, "", equipmentType, null);
    }
}
