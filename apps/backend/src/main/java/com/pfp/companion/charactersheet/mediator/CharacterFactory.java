package com.pfp.companion.charactersheet.mediator;

import com.pfp.companion.charactersheet.entity.Character;
import com.pfp.companion.charactersheet.entity.Ownership;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public final class CharacterFactory {

    public Character createForUser(String name, UUID userId) {
        return Character.createNew(name, Ownership.authenticated(userId));
    }

    public Character createLocalGuest(String name) {
        return Character.createNew(name, Ownership.localGuest());
    }
}
