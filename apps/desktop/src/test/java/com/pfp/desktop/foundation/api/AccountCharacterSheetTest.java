package com.pfp.desktop.foundation.api;

import static org.assertj.core.api.Assertions.assertThat;

import com.pfp.desktop.foundation.json.LocalCharacterSheet;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class AccountCharacterSheetTest {
    @Test
    void mapsServerSheetToDesktopSheetModel() {
        UUID characterId = UUID.randomUUID();
        UUID itemId = UUID.randomUUID();
        UUID spellId = UUID.randomUUID();
        AccountCharacterSheet serverSheet = new AccountCharacterSheet(
                characterId,
                "Server Hero",
                "https://example.test/hero.png",
                new AccountCharacterSheet.InfoResponse(3, "Origin", "Background", "Class", "Spec"),
                Map.of("strength", new AccountCharacterSheet.StatResponse(5, "d10")),
                List.of(new AccountCharacterSheet.SkillResponse("STRENGTH", "ATHLETICS", 2, "d6", "d10")),
                new AccountCharacterSheet.ConditionResponse(new BigDecimal("100"), 7, 3,
                        new BigDecimal("9"), new BigDecimal("40"), new BigDecimal("1"), false,
                        new AccountCharacterSheet.BodyHealthResponse(
                                part(60), part(40), part(100), part(60), part(60), part(60), part(60)
                        )),
                new AccountCharacterSheet.BlessingsResponse(1, 2),
                new AccountCharacterSheet.AdditionalInfoResponse("Look", "Origin detail", "Allies", "Primary", "Secondary"),
                new AccountCharacterSheet.InventoryResponse(new BigDecimal("40"), BigDecimal.ONE, false,
                        List.of(new AccountCharacterSheet.InventorySlotResponse(0,
                                new AccountCharacterSheet.ItemResponse(itemId, "ITEM", "Ring",
                                        "https://example.test/ring.png", BigDecimal.ONE, "A ring", "", BigDecimal.ZERO)))),
                Map.of("HEAD", new AccountCharacterSheet.EquippedItemResponse(itemId, "Ring", "")),
                new AccountCharacterSheet.MoneyResponse(new BigDecimal("56"), new BigDecimal("56"), "CURRENCY_1"),
                List.of(new AccountCharacterSheet.SpellResponse(spellId, "Spark", "SPELL", "PRIEST",
                        "", "Voice", "Small flash"))
        );

        LocalCharacterSheet sheet = serverSheet.toLocalSheet();

        assertThat(sheet.id()).isEqualTo(characterId.toString());
        assertThat(sheet.name()).isEqualTo("Server Hero");
        assertThat(sheet.info().level()).isEqualTo(3);
        assertThat(sheet.stats().strength()).isEqualTo(5);
        assertThat(sheet.inventory().items()).hasSize(1);
        assertThat(sheet.equipment()).contains(new LocalCharacterSheet.EquipmentSlot("HEAD", itemId.toString()));
        assertThat(sheet.spells().get(0).description()).isEqualTo("Small flash");
        assertThat(sheet.additionalInfo().notesPrimary()).isEqualTo("Primary");
    }

    private static AccountCharacterSheet.BodyPartHealthResponse part(int max) {
        return new AccountCharacterSheet.BodyPartHealthResponse(max, max);
    }
}
