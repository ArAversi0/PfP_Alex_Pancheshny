package com.pfp.companion.charactertransfer.mediator;

import com.pfp.companion.charactersheet.entity.Character;
import com.pfp.companion.charactersheet.entity.Ownership;
import com.pfp.companion.charactersheet.mediator.CharacterQueryService;
import com.pfp.companion.charactersheet.mediator.CharacterRepository;
import com.pfp.companion.charactersheet.mediator.CharacterSheetLimits;
import java.util.Objects;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public final class CharacterTransferApplicationService {

    private final CharacterJsonTransferService transferService;
    private final CharacterRepository repository;
    private final CharacterQueryService queryService;

    public CharacterTransferApplicationService(CharacterJsonTransferService transferService,
            CharacterRepository repository, CharacterQueryService queryService) {
        this.transferService = Objects.requireNonNull(transferService);
        this.repository = Objects.requireNonNull(repository);
        this.queryService = Objects.requireNonNull(queryService);
    }

    public String exportOwnedCharacter(UUID characterId, UUID userId) {
        return transferService.exportCharacter(queryService.findOwnedCharacter(characterId, userId));
    }

    public Character importAsNewCharacter(String json, UUID userId) {
        if (repository.countByUserId(userId) >= CharacterSheetLimits.AUTHENTICATED_USER) {
            throw new IllegalStateException("authenticated character limit reached");
        }
        Character character = transferService.importAsNew(json, Ownership.authenticated(userId));
        return repository.save(character);
    }
}
