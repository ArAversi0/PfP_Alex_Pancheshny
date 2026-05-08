package com.pfp.companion.charactersheet.mediator;

import java.util.List;
import java.util.UUID;

public record AdminCharacterGroup(UUID userId, String email, List<CharacterCard> characters) {
}
