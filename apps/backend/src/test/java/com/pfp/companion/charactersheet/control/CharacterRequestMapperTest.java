package com.pfp.companion.charactersheet.control;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.pfp.companion.charactersheet.control.CharacterAssetRequests.ItemUpsert;
import com.pfp.companion.charactersheet.control.CharacterAssetRequests.SpellUpsert;
import com.pfp.companion.charactersheet.control.CharacterSheetRequests.Skill;
import com.pfp.companion.charactersheet.entity.Item;
import com.pfp.companion.charactersheet.entity.ItemType;
import com.pfp.companion.charactersheet.entity.Spell;
import com.pfp.companion.charactersheet.entity.SpellClass;
import com.pfp.companion.charactersheet.entity.SkillName;
import com.pfp.companion.charactersheet.entity.SpellType;
import com.pfp.companion.charactersheet.entity.StatGroup;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;

class CharacterRequestMapperTest {

    private final CharacterRequestMapper mapper = new CharacterRequestMapper();

    @Test
    void rejectsDuplicateSkillNames() {
        Skill athletics = new Skill(StatGroup.STRENGTH, SkillName.ATHLETICS, 1);

        assertThatThrownBy(() -> mapper.toSkills(List.of(athletics, athletics)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("duplicate");
    }

    @Test
    void rejectsSkillWithMismatchedStatGroup() {
        Skill athletics = new Skill(StatGroup.DEXTERITY, SkillName.ATHLETICS, 1);

        assertThatThrownBy(() -> mapper.toSkills(List.of(athletics)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("statGroup");
    }

    @Test
    void mapsItemWithEmptyImageUrl() {
        ItemUpsert request = new ItemUpsert(ItemType.ITEM, "Rope", "", BigDecimal.ONE,
                "", null, null);

        Item item = mapper.toNewItem(request);

        assertThat(item.image()).isEmpty();
    }

    @Test
    void mapsSpellWithEmptyImageUrl() {
        SpellUpsert request = new SpellUpsert("Flame", SpellType.SPELL, SpellClass.PRIEST,
                "", "Hands free", "");

        Spell spell = mapper.toNewSpell(request);

        assertThat(spell.image()).isEmpty();
    }
}
