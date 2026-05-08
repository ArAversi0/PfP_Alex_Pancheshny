package com.pfp.companion.charactersheet.mediator;

import com.pfp.companion.charactersheet.entity.Character;
import com.pfp.companion.charactersheet.entity.Item;
import com.pfp.companion.charactersheet.entity.Ownership;
import com.pfp.companion.charactersheet.entity.Spell;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public final class CharacterQueryService {

    private final CharacterRepository repository;

    public CharacterQueryService(CharacterRepository repository) {
        this.repository = Objects.requireNonNull(repository);
    }

    public List<CharacterCard> findCardsForUser(UUID userId) {
        return repository.findCardsByUserId(Objects.requireNonNull(userId));
    }

    public Character findOwnedCharacter(UUID characterId, UUID userId) {
        Character character = repository.findById(Objects.requireNonNull(characterId))
                .orElseThrow(() -> new CharacterNotFoundException(characterId));
        if (character.ownership().type() != Ownership.Type.AUTHENTICATED
                || !Objects.equals(character.ownership().userId(), userId)) {
            throw new CharacterNotFoundException(characterId);
        }
        return character;
    }

    public Item findOwnedItem(UUID characterId, UUID userId, UUID itemId) {
        Item item = findOwnedCharacter(characterId, userId).inventory().items().get(itemId);
        if (item == null) {
            throw new CharacterSheetResourceNotFoundException("item", itemId);
        }
        return item;
    }

    public Spell findOwnedSpell(UUID characterId, UUID userId, UUID spellId) {
        Spell spell = findOwnedCharacter(characterId, userId).spells().get(spellId);
        if (spell == null) {
            throw new CharacterSheetResourceNotFoundException("spell", spellId);
        }
        return spell;
    }
}
