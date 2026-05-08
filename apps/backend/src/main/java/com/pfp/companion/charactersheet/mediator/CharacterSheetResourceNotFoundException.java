package com.pfp.companion.charactersheet.mediator;

import java.util.UUID;

public final class CharacterSheetResourceNotFoundException extends RuntimeException {

    public CharacterSheetResourceNotFoundException(String resource, UUID id) {
        super(resource + " not found: " + id);
    }
}
