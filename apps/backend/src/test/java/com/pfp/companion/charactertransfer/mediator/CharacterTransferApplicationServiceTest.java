package com.pfp.companion.charactertransfer.mediator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pfp.companion.charactersheet.entity.Character;
import com.pfp.companion.charactersheet.entity.Ownership;
import com.pfp.companion.charactersheet.mediator.CharacterQueryService;
import com.pfp.companion.charactersheet.mediator.CharacterRepository;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class CharacterTransferApplicationServiceTest {

    private final CharacterJsonTransferService jsonTransfer =
            new CharacterJsonTransferService(new ObjectMapper());
    private final CharacterRepository repository = mock(CharacterRepository.class);
    private final CharacterQueryService queryService = mock(CharacterQueryService.class);
    private final CharacterTransferApplicationService service =
            new CharacterTransferApplicationService(jsonTransfer, repository, queryService);

    @Test
    void importsCanonicalJsonAsNewCharacterOwnedByCurrentUser() {
        UUID userId = UUID.randomUUID();
        Character source = Character.createNew("Arden", Ownership.localGuest());
        when(repository.countByUserId(userId)).thenReturn(0L);
        when(repository.save(org.mockito.ArgumentMatchers.any())).thenAnswer(call -> call.getArgument(0));

        Character imported = service.importAsNewCharacter(jsonTransfer.exportCharacter(source), userId);

        assertThat(imported.id()).isNotEqualTo(source.id());
        assertThat(imported.ownership()).isEqualTo(Ownership.authenticated(userId));
        verify(repository).save(imported);
    }

    @Test
    void rejectsImportAtAuthenticatedCharacterLimit() {
        UUID userId = UUID.randomUUID();
        when(repository.countByUserId(userId)).thenReturn(100L);

        assertThatThrownBy(() -> service.importAsNewCharacter("{}", userId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("limit");
    }

    @Test
    void exportsOnlyOwnedCharacter() {
        UUID userId = UUID.randomUUID();
        Character source = Character.createNew("Arden", Ownership.authenticated(userId));
        when(queryService.findOwnedCharacter(source.id(), userId)).thenReturn(source);

        assertThat(service.exportOwnedCharacter(source.id(), userId))
                .contains("\"name\" : \"Arden\"");
    }
}
