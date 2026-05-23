package com.pfp.companion.charactersheet.mediator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.pfp.companion.charactersheet.entity.Character;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class CharacterQueryServiceTest {

    private final CharacterRepository repository = mock(CharacterRepository.class);
    private final CharacterQueryService service = new CharacterQueryService(repository);
    private final CharacterFactory factory = new CharacterFactory();

    @Test
    void returnsCharacterOwnedByCurrentUser() {
        UUID userId = UUID.randomUUID();
        Character character = factory.createForUser("Arden", userId);
        when(repository.findById(character.id())).thenReturn(Optional.of(character));

        assertThat(service.findOwnedCharacter(character.id(), userId)).isSameAs(character);
    }

    @Test
    void hidesCharacterOwnedByAnotherUser() {
        UUID ownerId = UUID.randomUUID();
        Character character = factory.createForUser("Arden", ownerId);
        when(repository.findById(character.id())).thenReturn(Optional.of(character));

        assertThatThrownBy(() -> service.findOwnedCharacter(character.id(), UUID.randomUUID()))
                .isInstanceOf(CharacterNotFoundException.class);
    }
}
