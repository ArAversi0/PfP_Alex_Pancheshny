package com.pfp.companion.identityaccess.mediator;

import com.pfp.companion.identityaccess.entity.Role;
import java.time.Instant;
import java.util.UUID;

public record AdminUserSummary(UUID id, String email, Role role, boolean emailVerified,
        Instant createdAt, long characterCount) {
}
