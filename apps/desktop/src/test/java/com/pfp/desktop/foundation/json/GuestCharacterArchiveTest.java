package com.pfp.desktop.foundation.json;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class GuestCharacterArchiveTest {

    @TempDir
    Path tempDir;

    @Test
    void createsAndReloadsGuestCharacters() throws Exception {
        GuestCharacterArchive archive = new GuestCharacterArchive(tempDir);
        archive.initialize();

        LocalCharacterRecord created = archive.createCharacter("Arden");

        GuestCharacterArchive reloaded = new GuestCharacterArchive(tempDir);
        reloaded.initialize();

        assertThat(reloaded.list()).hasSize(1);
        assertThat(reloaded.list().get(0).id()).isEqualTo(created.id());
        assertThat(reloaded.list().get(0).name()).isEqualTo("Arden");
        assertThat(reloaded.readCharacterJson(created.id()))
                .contains("\"schemaVersion\" : \"1.0\"")
                .contains("\"character\"");
    }

    @Test
    void importsCanonicalCharacterJsonAsNewLocalRecord() throws Exception {
        GuestCharacterArchive sourceArchive = new GuestCharacterArchive(tempDir.resolve("source"));
        sourceArchive.initialize();
        LocalCharacterRecord source = sourceArchive.createCharacter("Imported Hero");
        Path sourceFile = tempDir.resolve("hero.json");
        sourceArchive.exportCharacter(source.id(), sourceFile);

        GuestCharacterArchive targetArchive = new GuestCharacterArchive(tempDir.resolve("target"));
        targetArchive.initialize();
        LocalCharacterRecord imported = targetArchive.importCharacter(sourceFile);

        assertThat(imported.id()).isNotEqualTo(source.id());
        assertThat(imported.name()).isEqualTo("Imported Hero");
        assertThat(targetArchive.readCharacterJson(imported.id())).contains("Imported Hero");
    }

    @Test
    void savesEditableCharacterSheetFieldsToLocalJson() throws Exception {
        GuestCharacterArchive archive = new GuestCharacterArchive(tempDir);
        archive.initialize();
        LocalCharacterRecord created = archive.createCharacter("Draft");
        LocalCharacterSheet original = archive.readCharacterSheet(created.id());

        assertThat(original.condition().hp().head().current()).isEqualTo(60);
        assertThat(original.condition().hp().neck().max()).isEqualTo(40);
        assertThat(original.condition().hp().torso().current()).isEqualTo(100);
        assertThat(original.condition().hp().leftArm().max()).isEqualTo(60);
        assertThat(original.inventory().slotCount()).isEqualTo(10);

        LocalCharacterSheet edited = new LocalCharacterSheet(
                original.id(),
                "Edited Hero",
                "https://example.test/hero.png",
                new LocalCharacterSheet.Info(3, "Northern", "Mercenary", "Warrior", "Vanguard"),
                new LocalCharacterSheet.Stats(4, 5, 6, 7, 8, 9, 10),
                original.skills(),
                new LocalCharacterSheet.Condition(original.condition().hp(), 2, new BigDecimal("7.5"), new BigDecimal("42.25")),
                new LocalCharacterSheet.Blessings(1, 2),
                new LocalCharacterSheet.Money(new BigDecimal("100"), "BAD_CURRENCY"),
                new LocalCharacterSheet.Inventory(
                        java.util.List.of(new LocalCharacterSheet.InventoryItem(
                                "item-1",
                                "EQUIPMENT",
                                "Iron Helm",
                                "",
                                new BigDecimal("3.5"),
                                "Scratched but reliable",
                                "HEAD",
                                BigDecimal.ZERO
                        )),
                        java.util.List.of(
                                new LocalCharacterSheet.InventorySlot(0, "item-1"),
                                new LocalCharacterSheet.InventorySlot(1, "")
                        )
                ),
                java.util.List.of(new LocalCharacterSheet.EquipmentSlot("HEAD", "item-1")),
                java.util.List.of(new LocalCharacterSheet.SpellPreview(
                        "spell-1",
                        "Spark",
                        "SPELL",
                        "PRIEST",
                        "Voice",
                        "https://example.test/spark.png",
                        "A small flash of light"
                )),
                new LocalCharacterSheet.AdditionalInfo("Tall", "Born near the coast", "Guild", "Primary", "Secondary")
        );

        LocalCharacterRecord updated = archive.saveCharacterSheet(edited);
        LocalCharacterSheet reloaded = archive.readCharacterSheet(created.id());

        assertThat(updated.name()).isEqualTo("Edited Hero");
        assertThat(updated.level()).isEqualTo(3);
        assertThat(updated.classLine()).isEqualTo("Warrior / Vanguard");
        assertThat(reloaded.name()).isEqualTo("Edited Hero");
        assertThat(reloaded.info().origin()).isEqualTo("Northern");
        assertThat(reloaded.stats().mind()).isEqualTo(10);
        assertThat(reloaded.condition().movementSpeed()).isEqualByComparingTo("7.5");
        assertThat(reloaded.money().displayCurrency()).isEqualTo("CURRENCY_1");
        assertThat(reloaded.inventory().itemCount()).isEqualTo(1);
        assertThat(reloaded.inventory().currentWeight()).isEqualByComparingTo("3.5");
        assertThat(reloaded.inventory().slots().get(0).itemId()).isEqualTo("item-1");
        assertThat(reloaded.equipment()).contains(new LocalCharacterSheet.EquipmentSlot("HEAD", "item-1"));
        assertThat(reloaded.spells()).hasSize(1);
        assertThat(reloaded.spells().get(0).description()).isEqualTo("A small flash of light");
        assertThat(reloaded.blessings().inspirations()).isEqualTo(2);
        assertThat(reloaded.additionalInfo().allies()).isEqualTo("Guild");
        assertThat(archive.readCharacterJson(created.id()))
                .contains("\"name\" : \"Edited Hero\"")
                .contains("\"name\" : \"Spark\"")
                .contains("\"description\" : \"A small flash of light\"");
    }

    @Test
    void rejectsImportsWithUnsupportedSchema() throws Exception {
        GuestCharacterArchive archive = new GuestCharacterArchive(tempDir);
        archive.initialize();
        Path json = tempDir.resolve("bad.json");
        Files.writeString(json, "{\"schemaVersion\":\"2.0\",\"character\":{\"name\":\"Bad\"}}");

        assertThatThrownBy(() -> archive.importCharacter(json))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("schema 1.0");
    }

    @Test
    void enforcesGuestCharacterLimit() throws Exception {
        GuestCharacterArchive archive = new GuestCharacterArchive(tempDir);
        archive.initialize();
        for (int index = 0; index < GuestCharacterArchive.CHARACTER_LIMIT; index++) {
            archive.createCharacter("Hero " + index);
        }

        assertThatThrownBy(() -> archive.createCharacter("Overflow"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("limit");
    }
}
