package com.pfp.companion.charactersheet.mediator;

import com.pfp.companion.charactersheet.entity.Character;
import java.util.Objects;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public final class CreateCharacterService {

    private final CharacterRepository repository;
    private final CharacterFactory factory;

    public CreateCharacterService(CharacterRepository repository, CharacterFactory factory) {
        this.repository = Objects.requireNonNull(repository);
        this.factory = Objects.requireNonNull(factory);
    }

    public Character createForUser(String name, UUID userId) {
        if (repository.countByUserId(userId) >= CharacterSheetLimits.AUTHENTICATED_USER) {
            throw new IllegalStateException("authenticated character limit reached");
        }
        return repository.save(factory.createForUser(name, userId));
    }
}
