package com.pfp.companion.charactersheet.mediator;

import com.pfp.companion.charactersheet.entity.Character;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CharacterRepository {

    long countByUserId(UUID userId);

    Character save(Character character);

    Optional<Character> findById(UUID id);

    List<CharacterCard> findCardsByUserId(UUID userId);

    void deleteById(UUID id);
}
