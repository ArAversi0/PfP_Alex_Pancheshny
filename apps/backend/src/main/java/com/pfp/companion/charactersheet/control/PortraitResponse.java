package com.pfp.companion.charactersheet.control;

import java.util.UUID;

public record PortraitResponse(UUID characterId, String imageUrl) {
}
