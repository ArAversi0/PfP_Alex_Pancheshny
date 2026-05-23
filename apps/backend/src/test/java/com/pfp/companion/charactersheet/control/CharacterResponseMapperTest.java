package com.pfp.companion.charactersheet.control;

import static org.assertj.core.api.Assertions.assertThat;

import com.pfp.companion.charactersheet.entity.Character;
import com.pfp.companion.charactersheet.entity.CharacterCondition;
import com.pfp.companion.charactersheet.entity.CharacterStats;
import com.pfp.companion.charactersheet.entity.SkillName;
import com.pfp.companion.charactersheet.entity.StatGroup;
import com.pfp.companion.charactersheet.mediator.CharacterFactory;
import com.pfp.gamerules.BodyPart;
import com.pfp.gamerules.BodyPartHealth;
import java.math.BigDecimal;
import java.util.EnumMap;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class CharacterResponseMapperTest {

    private final CharacterResponseMapper mapper = new CharacterResponseMapper();

    @Test
    void mapsInitialSheetWithBackendDerivedValues() {
        Character character = new CharacterFactory().createForUser("Arden", UUID.randomUUID());

        CharacterSheetResponse response = mapper.toSheet(character);

        assertThat(response.name()).isEqualTo("Arden");
        assertThat(response.stats().get("strength").roll()).isEqualTo("3");
        assertThat(response.skills()).hasSize(30);
        assertThat(response.condition().globalHealthPercent()).isEqualByComparingTo("100.00");
        assertThat(response.condition().passiveDodge()).isEqualTo(3);
        assertThat(response.condition().currentCarryWeight()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(response.condition().overweight()).isFalse();
        assertThat(response.equipment()).containsEntry("HEAD", null);
        assertThat(response.money().displayCurrency()).isEqualTo("CURRENCY_1");
    }

    @Test
    void recalculatesDerivedValuesFromUpdatedRawState() {
        Character character = new CharacterFactory().createForUser("Arden", UUID.randomUUID());
        character.updateStats(new CharacterStats(Map.of(
                StatGroup.STRENGTH, 4, StatGroup.DEXTERITY, 4, StatGroup.STAMINA, 0,
                StatGroup.INTELLIGENCE, 0, StatGroup.CHARISMA, 0, StatGroup.LUCK, 0,
                StatGroup.MIND, 0)));
        character.updateSkills(Map.of(SkillName.ATHLETICS, 2));
        character.updateCondition(new CharacterCondition(damagedHead(), 5,
                BigDecimal.TEN, BigDecimal.valueOf(120)));

        CharacterSheetResponse response = mapper.toSheet(character);

        assertThat(response.stats().get("dexterity").roll()).isEqualTo("3d10");
        assertThat(response.skills()).filteredOn(skill -> skill.skillName().equals("ATHLETICS"))
                .singleElement().satisfies(skill -> assertThat(skill.effectiveRoll()).isEqualTo("3d8"));
        assertThat(response.condition().globalHealthPercent()).isEqualByComparingTo("50.00");
        assertThat(response.condition().passiveDodge()).isEqualTo(10);
    }

    private static Map<BodyPart, BodyPartHealth> damagedHead() {
        EnumMap<BodyPart, BodyPartHealth> hp = new EnumMap<>(BodyPart.class);
        hp.put(BodyPart.HEAD, new BodyPartHealth(60, 30));
        hp.put(BodyPart.NECK, new BodyPartHealth(40, 40));
        hp.put(BodyPart.TORSO, new BodyPartHealth(100, 100));
        hp.put(BodyPart.LEFT_ARM, new BodyPartHealth(60, 60));
        hp.put(BodyPart.RIGHT_ARM, new BodyPartHealth(60, 60));
        hp.put(BodyPart.LEFT_LEG, new BodyPartHealth(60, 60));
        hp.put(BodyPart.RIGHT_LEG, new BodyPartHealth(60, 60));
        return hp;
    }
}
