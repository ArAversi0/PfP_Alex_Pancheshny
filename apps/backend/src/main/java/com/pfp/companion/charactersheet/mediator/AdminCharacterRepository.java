package com.pfp.companion.charactersheet.mediator;

import com.pfp.companion.charactersheet.entity.Character;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AdminCharacterRepository {

    List<AdminCharacterGroup> findAllGroupedByOwner();

    Optional<Character> findById(UUID characterId);
}
