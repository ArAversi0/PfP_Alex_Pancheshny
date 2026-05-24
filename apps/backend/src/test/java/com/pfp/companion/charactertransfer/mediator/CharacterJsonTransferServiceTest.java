package com.pfp.companion.charactertransfer.mediator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pfp.companion.charactersheet.entity.Character;
import com.pfp.companion.charactersheet.entity.EquipmentSlotCode;
import com.pfp.companion.charactersheet.entity.EquipmentType;
import com.pfp.companion.charactersheet.entity.Item;
import com.pfp.companion.charactersheet.entity.ItemType;
import com.pfp.companion.charactersheet.entity.Ownership;
import java.math.BigDecimal;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class CharacterJsonTransferServiceTest {

    private final CharacterJsonTransferService service =
            new CharacterJsonTransferService(new ObjectMapper());

    @Test
    void roundTripsRawCharacterStateAsNewLocalGuest() {
        Character original = Character.createNew("Arden", Ownership.localGuest());
        Item helmet = new Item(UUID.randomUUID(), ItemType.EQUIPMENT, "Helmet", "helmet.jpg",
                new BigDecimal("1.25"), "", EquipmentType.HEAD, null);
        original.inventory().add(helmet, 0);
        original.inventory().ensureSlot(1);
        original.equip(EquipmentSlotCode.HEAD, helmet.id());

        String json = service.exportCharacter(original);
        Character imported = service.importAsNewLocalGuest(json);

        assertThat(json).contains("\"schemaVersion\" : \"1.0\"");
        assertThat(json).contains("\"class\" : \"\"");
        assertThat(json).doesNotContain("className");
        assertThat(imported.id()).isNotEqualTo(original.id());
        assertThat(imported.name()).isEqualTo("Arden");
        assertThat(imported.ownership()).isEqualTo(Ownership.localGuest());
        assertThat(imported.currentCarryWeight()).isEqualByComparingTo("1.25");
        assertThat(imported.inventory().slots()).containsKey(1);
        assertThat(imported.equipment().get(EquipmentSlotCode.HEAD).itemId()).isEqualTo(helmet.id());
    }

    @Test
    void rejectsUnknownSchemaVersion() {
        String json = service.exportCharacter(Character.createNew("Arden", Ownership.localGuest()))
                .replace("\"1.0\"", "\"2.0\"");

        assertThatThrownBy(() -> service.importAsNewLocalGuest(json))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("unsupported schemaVersion");
    }
}
