package com.pfp.companion.charactersheet.mediator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.pfp.companion.charactersheet.entity.Character;
import com.pfp.companion.charactersheet.entity.CharacterStats;
import com.pfp.companion.charactersheet.entity.StatGroup;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class CharacterSheetMutationServiceTest {

    private final CharacterRepository repository = mock(CharacterRepository.class);
    private final CharacterQueryService queryService = mock(CharacterQueryService.class);
    private final CharacterSheetMutationService service =
            new CharacterSheetMutationService(repository, queryService);

    @Test
    void updatesOwnedAggregateAndSavesIt() {
        UUID userId = UUID.randomUUID();
        Character character = new CharacterFactory().createForUser("Arden", userId);
        CharacterStats stats = new CharacterStats(Map.of(
                StatGroup.STRENGTH, 1, StatGroup.DEXTERITY, 2, StatGroup.STAMINA, 3,
                StatGroup.INTELLIGENCE, 4, StatGroup.CHARISMA, 5, StatGroup.LUCK, 6,
                StatGroup.MIND, 7));
        when(queryService.findOwnedCharacter(character.id(), userId)).thenReturn(character);
        when(repository.save(character)).thenReturn(character);

        Character updated = service.updateStats(character.id(), userId, stats);

        assertThat(updated.stats().level(StatGroup.MIND)).isEqualTo(7);
        verify(repository).save(character);
    }

    @Test
    void verifiesOwnershipBeforeDeletingCharacter() {
        UUID userId = UUID.randomUUID();
        Character character = new CharacterFactory().createForUser("Arden", userId);
        when(queryService.findOwnedCharacter(character.id(), userId)).thenReturn(character);

        service.deleteCharacter(character.id(), userId);

        verify(repository).deleteById(character.id());
    }

    @Test
    void addsInventoryRowsAndSavesAggregate() {
        UUID userId = UUID.randomUUID();
        Character character = new CharacterFactory().createForUser("Arden", userId);
        when(queryService.findOwnedCharacter(character.id(), userId)).thenReturn(character);
        when(repository.save(character)).thenReturn(character);

        Character updated = service.addInventoryRows(character.id(), userId, 2);

        assertThat(updated.inventory().slots()).hasSize(30);
        verify(repository).save(character);
    }
}
