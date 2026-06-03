package com.pfp.desktop.foundation.api;

import java.util.UUID;

public record AuthUser(UUID id, String email, String role, boolean emailVerified) {
}
