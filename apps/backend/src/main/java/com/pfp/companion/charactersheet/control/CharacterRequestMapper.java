package com.pfp.companion.charactersheet.control;

import com.pfp.companion.charactersheet.control.CharacterSheetRequests.BodyPartHealth;
import com.pfp.companion.charactersheet.entity.AdditionalInfo;
import com.pfp.companion.charactersheet.entity.BlessingInspiration;
import com.pfp.companion.charactersheet.entity.CharacterCondition;
import com.pfp.companion.charactersheet.entity.CharacterInfo;
import com.pfp.companion.charactersheet.entity.CharacterStats;
import com.pfp.companion.charactersheet.entity.Item;
import com.pfp.companion.charactersheet.entity.SkillName;
import com.pfp.companion.charactersheet.entity.Spell;
import com.pfp.companion.charactersheet.entity.StatGroup;
import com.pfp.gamerules.BodyPart;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public final class CharacterRequestMapper {

    public CharacterInfo toInfo(CharacterSheetRequests.Info request) {
        return new CharacterInfo(request.level(), request.origin(), request.background(),
                request.className(), request.specialization());
    }

    public CharacterStats toStats(CharacterSheetRequests.Stats request) {
        return new CharacterStats(Map.of(
                StatGroup.STRENGTH, request.strength(),
                StatGroup.DEXTERITY, request.dexterity(),
                StatGroup.STAMINA, request.stamina(),
                StatGroup.INTELLIGENCE, request.intelligence(),
                StatGroup.CHARISMA, request.charisma(),
                StatGroup.LUCK, request.luck(),
                StatGroup.MIND, request.mind()));
    }

    public Map<SkillName, Integer> toSkills(List<CharacterSheetRequests.Skill> requests) {
        EnumMap<SkillName, Integer> result = new EnumMap<>(SkillName.class);
        for (CharacterSheetRequests.Skill request : requests) {
            if (request.skillName().statGroup() != request.statGroup()) {
                throw new IllegalArgumentException("skill statGroup does not match skillName");
            }
            if (result.putIfAbsent(request.skillName(), request.level()) != null) {
                throw new IllegalArgumentException("skill request contains duplicate skillName");
            }
        }
        return result;
    }

    public CharacterCondition toCondition(CharacterSheetRequests.Condition request) {
        EnumMap<BodyPart, com.pfp.gamerules.BodyPartHealth> hp = new EnumMap<>(BodyPart.class);
        hp.put(BodyPart.HEAD, toHp(request.hp().head()));
        hp.put(BodyPart.NECK, toHp(request.hp().neck()));
        hp.put(BodyPart.TORSO, toHp(request.hp().torso()));
        hp.put(BodyPart.LEFT_ARM, toHp(request.hp().leftArm()));
        hp.put(BodyPart.RIGHT_ARM, toHp(request.hp().rightArm()));
        hp.put(BodyPart.LEFT_LEG, toHp(request.hp().leftLeg()));
        hp.put(BodyPart.RIGHT_LEG, toHp(request.hp().rightLeg()));
        return new CharacterCondition(hp, request.passiveDefense(), request.movementSpeed(),
                request.maxCarryWeight());
    }

    public BlessingInspiration toBlessings(CharacterSheetRequests.Blessings request) {
        return new BlessingInspiration(request.blessings(), request.inspirations());
    }

    public AdditionalInfo toAdditionalInfo(CharacterSheetRequests.AdditionalInfo request) {
        return new AdditionalInfo(request.appearance(), request.detailedOrigin(), request.allies(),
                request.notesPrimary(), request.notesSecondary());
    }

    public Item toNewItem(CharacterAssetRequests.ItemUpsert request) {
        return toItem(UUID.randomUUID(), request);
    }

    public Item toItem(UUID itemId, CharacterAssetRequests.ItemUpsert request) {
        return new Item(itemId, request.type(), request.title(), request.imageUrl(),
                request.weight(), request.description(), request.equipmentType(),
                request.sellPriceBase());
    }

    public Spell toNewSpell(CharacterAssetRequests.SpellUpsert request) {
        return toSpell(UUID.randomUUID(), request);
    }

    public Spell toSpell(UUID spellId, CharacterAssetRequests.SpellUpsert request) {
        return new Spell(spellId, request.name(), request.type(), request.spellClass(),
                request.imageUrl(), request.requirements(), request.description());
    }

    private static com.pfp.gamerules.BodyPartHealth toHp(BodyPartHealth request) {
        return new com.pfp.gamerules.BodyPartHealth(request.max(), request.current());
    }
}
