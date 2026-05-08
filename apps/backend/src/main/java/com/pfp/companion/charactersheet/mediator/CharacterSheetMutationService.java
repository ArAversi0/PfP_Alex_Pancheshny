package com.pfp.companion.charactersheet.mediator;

import com.pfp.companion.charactersheet.entity.AdditionalInfo;
import com.pfp.companion.charactersheet.entity.BlessingInspiration;
import com.pfp.companion.charactersheet.entity.Character;
import com.pfp.companion.charactersheet.entity.CharacterCondition;
import com.pfp.companion.charactersheet.entity.CharacterInfo;
import com.pfp.companion.charactersheet.entity.CharacterStats;
import com.pfp.companion.charactersheet.entity.Currency;
import com.pfp.companion.charactersheet.entity.EquipmentSlotCode;
import com.pfp.companion.charactersheet.entity.Item;
import com.pfp.companion.charactersheet.entity.SkillName;
import com.pfp.companion.charactersheet.entity.Spell;
import java.math.BigDecimal;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Consumer;
import org.springframework.stereotype.Service;

@Service
public final class CharacterSheetMutationService {

    private final CharacterRepository repository;
    private final CharacterQueryService queryService;

    public CharacterSheetMutationService(CharacterRepository repository,
            CharacterQueryService queryService) {
        this.repository = Objects.requireNonNull(repository);
        this.queryService = Objects.requireNonNull(queryService);
    }

    public Character updateInfo(UUID characterId, UUID userId, String name, CharacterInfo info) {
        return update(characterId, userId, character -> character.updateInfo(name, info));
    }

    public Character updateImage(UUID characterId, UUID userId, String imageUrl) {
        return update(characterId, userId, character -> character.updateImage(imageUrl));
    }

    public Character updateStats(UUID characterId, UUID userId, CharacterStats stats) {
        return update(characterId, userId, character -> character.updateStats(stats));
    }

    public Character updateSkills(UUID characterId, UUID userId, Map<SkillName, Integer> levels) {
        return update(characterId, userId, character -> character.updateSkills(levels));
    }

    public Character updateCondition(UUID characterId, UUID userId, CharacterCondition condition) {
        return update(characterId, userId, character -> character.updateCondition(condition));
    }

    public Character updateBlessings(UUID characterId, UUID userId,
            BlessingInspiration blessings) {
        return update(characterId, userId, character -> character.updateBlessings(blessings));
    }

    public Character updateAdditionalInfo(UUID characterId, UUID userId,
            AdditionalInfo additionalInfo) {
        return update(characterId, userId,
                character -> character.updateAdditionalInfo(additionalInfo));
    }

    public ItemPlacement addItem(UUID characterId, UUID userId, Item item) {
        Character character = queryService.findOwnedCharacter(characterId, userId);
        int slotIndex = character.addItem(item);
        repository.save(character);
        return new ItemPlacement(slotIndex, item);
    }

    public Character addInventoryRows(UUID characterId, UUID userId, int rowsToAdd) {
        return update(characterId, userId, character -> character.addInventoryRows(rowsToAdd));
    }

    public Character removeInventoryRow(UUID characterId, UUID userId) {
        return update(characterId, userId, Character::removeInventoryRow);
    }

    public Character moveInventoryItem(UUID characterId, UUID userId, int fromSlotIndex,
            int toSlotIndex) {
        return update(characterId, userId,
                character -> character.moveInventoryItem(fromSlotIndex, toSlotIndex));
    }

    public Character updateItem(UUID characterId, UUID userId, Item item) {
        return update(characterId, userId, character -> {
            requireItem(character, item.id());
            character.updateItem(item);
        });
    }

    public Character throwAwayItem(UUID characterId, UUID userId, UUID itemId) {
        return update(characterId, userId, character -> {
            requireItem(character, itemId);
            character.throwAwayItem(itemId);
        });
    }

    public Character sellTradeItem(UUID characterId, UUID userId, UUID itemId) {
        return update(characterId, userId, character -> {
            requireItem(character, itemId);
            character.sellTradeItem(itemId);
        });
    }

    public Character equipItem(UUID characterId, UUID userId, UUID itemId,
            EquipmentSlotCode slotCode) {
        return update(characterId, userId, character -> {
            requireItem(character, itemId);
            character.equip(slotCode, itemId);
        });
    }

    public Character unequipItem(UUID characterId, UUID userId, EquipmentSlotCode slotCode) {
        return update(characterId, userId, character -> character.unequip(slotCode));
    }

    public Character selectDisplayCurrency(UUID characterId, UUID userId, Currency currency) {
        return update(characterId, userId, character -> character.selectDisplayCurrency(currency));
    }

    public Character setMoneyAmountBase(UUID characterId, UUID userId, BigDecimal amountBase) {
        return update(characterId, userId, character -> character.setMoneyAmountBase(amountBase));
    }

    public Spell addSpell(UUID characterId, UUID userId, Spell spell) {
        Character character = queryService.findOwnedCharacter(characterId, userId);
        character.addSpell(spell);
        repository.save(character);
        return spell;
    }

    public Spell updateSpell(UUID characterId, UUID userId, Spell spell) {
        Character character = queryService.findOwnedCharacter(characterId, userId);
        requireSpell(character, spell.id());
        character.updateSpell(spell);
        repository.save(character);
        return spell;
    }

    public Character deleteSpell(UUID characterId, UUID userId, UUID spellId) {
        return update(characterId, userId, character -> {
            requireSpell(character, spellId);
            character.deleteSpell(spellId);
        });
    }

    public void deleteCharacter(UUID characterId, UUID userId) {
        Character character = queryService.findOwnedCharacter(characterId, userId);
        repository.deleteById(character.id());
    }

    private Character update(UUID characterId, UUID userId, Consumer<Character> mutation) {
        Character character = queryService.findOwnedCharacter(characterId, userId);
        mutation.accept(character);
        return repository.save(character);
    }

    private static void requireItem(Character character, UUID itemId) {
        if (!character.inventory().items().containsKey(itemId)) {
            throw new CharacterSheetResourceNotFoundException("item", itemId);
        }
    }

    private static void requireSpell(Character character, UUID spellId) {
        if (!character.spells().containsKey(spellId)) {
            throw new CharacterSheetResourceNotFoundException("spell", spellId);
        }
    }
}
