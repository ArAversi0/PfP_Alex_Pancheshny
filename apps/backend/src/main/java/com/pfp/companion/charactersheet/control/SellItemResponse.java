package com.pfp.companion.charactersheet.control;

import com.pfp.companion.charactersheet.control.CharacterSheetResponse.MoneyResponse;

public record SellItemResponse(boolean deleted, MoneyResponse money) {
}
