package com.pfp.companion.charactersheet.control;

import com.pfp.companion.charactersheet.entity.SkillName;
import com.pfp.companion.charactersheet.entity.StatGroup;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public final class CharacterSheetRequests {

    private CharacterSheetRequests() {
    }

    public record Info(@NotBlank @Size(max = 120) String name, @NotNull @Positive Integer level,
            @NotNull @Size(max = 120) String origin,
            @NotNull @Size(max = 120) String background,
            @NotNull @Size(max = 120) String className,
            @NotNull @Size(max = 120) String specialization) {
    }

    public record Portrait(@NotNull @Size(max = 500) String imageUrl) {
    }

    public record Stats(@NotNull @PositiveOrZero Integer strength,
            @NotNull @PositiveOrZero Integer dexterity,
            @NotNull @PositiveOrZero Integer stamina,
            @NotNull @PositiveOrZero Integer intelligence,
            @NotNull @PositiveOrZero Integer charisma,
            @NotNull @PositiveOrZero Integer luck,
            @NotNull @PositiveOrZero Integer mind) {
    }

    public record Skill(@NotNull StatGroup statGroup, @NotNull SkillName skillName,
            @NotNull @PositiveOrZero Integer level) {
    }

    public record Condition(@NotNull @PositiveOrZero Integer passiveDefense,
            @NotNull @DecimalMin("0.0") BigDecimal movementSpeed,
            @NotNull @DecimalMin("0.0") BigDecimal maxCarryWeight,
            @NotNull @Valid BodyHealth hp) {
    }

    public record BodyHealth(@NotNull @Valid BodyPartHealth head,
            @NotNull @Valid BodyPartHealth neck, @NotNull @Valid BodyPartHealth torso,
            @NotNull @Valid BodyPartHealth leftArm, @NotNull @Valid BodyPartHealth rightArm,
            @NotNull @Valid BodyPartHealth leftLeg, @NotNull @Valid BodyPartHealth rightLeg) {
    }

    public record BodyPartHealth(@NotNull @PositiveOrZero Integer max,
            @NotNull @PositiveOrZero Integer current) {
    }

    public record Blessings(@NotNull @PositiveOrZero Integer blessings,
            @NotNull @PositiveOrZero Integer inspirations) {
    }

    public record AdditionalInfo(@NotNull String appearance, @NotNull String detailedOrigin,
            @NotNull String allies, @NotNull String notesPrimary, @NotNull String notesSecondary) {
    }
}
