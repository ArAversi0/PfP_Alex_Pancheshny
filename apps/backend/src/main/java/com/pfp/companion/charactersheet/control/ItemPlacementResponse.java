package com.pfp.companion.charactersheet.control;

import com.pfp.companion.charactersheet.control.CharacterSheetResponse.ItemResponse;

public record ItemPlacementResponse(int slotIndex, ItemResponse item) {
}
