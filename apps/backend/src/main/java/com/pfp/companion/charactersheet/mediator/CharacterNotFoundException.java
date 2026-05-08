package com.pfp.companion.charactersheet.mediator;

import java.util.UUID;

public final class CharacterNotFoundException extends RuntimeException {

    public CharacterNotFoundException(UUID characterId) {
        super("character not found: " + characterId);
    }
}
