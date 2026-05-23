package com.pfp.companion.charactersheet.mediator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.pfp.companion.charactersheet.entity.Character;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class CreateCharacterServiceTest {

    private final CharacterRepository repository = mock(CharacterRepository.class);
    private final CreateCharacterService service =
            new CreateCharacterService(repository, new CharacterFactory());

    @Test
    void createsAndSavesCharacterBelowLimit() {
        UUID userId = UUID.randomUUID();
        when(repository.countByUserId(userId)).thenReturn(99L);
        when(repository.save(org.mockito.ArgumentMatchers.any())).thenAnswer(call -> call.getArgument(0));

        Character character = service.createForUser("Arden", userId);

        assertThat(character.name()).isEqualTo("Arden");
        verify(repository).save(character);
    }

    @Test
    void rejectsCharacterAboveAuthenticatedLimit() {
        UUID userId = UUID.randomUUID();
        when(repository.countByUserId(userId)).thenReturn(100L);

        assertThatThrownBy(() -> service.createForUser("Arden", userId))
                .isInstanceOf(IllegalStateException.class);
    }
}

