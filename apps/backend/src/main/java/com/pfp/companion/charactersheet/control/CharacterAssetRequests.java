package com.pfp.companion.charactersheet.control;

import com.pfp.companion.charactersheet.entity.Currency;
import com.pfp.companion.charactersheet.entity.EquipmentSlotCode;
import com.pfp.companion.charactersheet.entity.EquipmentType;
import com.pfp.companion.charactersheet.entity.ItemType;
import com.pfp.companion.charactersheet.entity.SpellClass;
import com.pfp.companion.charactersheet.entity.SpellType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.util.UUID;

public final class CharacterAssetRequests {

    private CharacterAssetRequests() {
    }

    public record AddInventoryRows(@NotNull @Positive Integer rowsToAdd) {
    }

    public record MoveInventoryItem(@NotNull @PositiveOrZero Integer fromSlotIndex,
            @NotNull @PositiveOrZero Integer toSlotIndex) {
    }

    public record ItemUpsert(@NotNull ItemType type, @NotBlank @Size(max = 150) String title,
            @NotNull @Size(max = 500) String imageUrl,
            @NotNull @DecimalMin("0.0") BigDecimal weight, String description,
            EquipmentType equipmentType, @DecimalMin("0.0") BigDecimal sellPriceBase) {
    }

    public record EquipItem(@NotNull UUID itemId, @NotNull EquipmentSlotCode slotCode) {
    }

    public record UnequipItem(@NotNull EquipmentSlotCode slotCode) {
    }

    public record SelectCurrency(@NotNull Currency displayCurrency) {
    }

    public record SetMoneyAmount(@NotNull @DecimalMin("0.0") BigDecimal amountBase) {
    }

    public record SpellUpsert(@NotBlank @Size(max = 150) String name, @NotNull SpellType type,
            @NotNull SpellClass spellClass, @NotNull @Size(max = 500) String imageUrl,
            @NotBlank String requirements, String description) {
    }
}
