package com.pfp.companion.charactersheet.control;

import java.util.UUID;

public record CharacterSummaryResponse(UUID id, String name, String imageUrl,
        CharacterInfoResponse info) {
}
