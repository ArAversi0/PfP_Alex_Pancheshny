package com.pfp.companion.charactersheet.mediator;

import com.pfp.companion.charactersheet.entity.Character;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AdminCharacterService {

    private final AdminCharacterRepository repository;

    public AdminCharacterService(AdminCharacterRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public List<AdminCharacterGroup> findAllGroupedByOwner() {
        return repository.findAllGroupedByOwner();
    }

    @Transactional(readOnly = true)
    public Character findCharacter(UUID characterId) {
        return repository.findById(characterId).orElseThrow(() -> new CharacterNotFoundException(characterId));
    }
}
