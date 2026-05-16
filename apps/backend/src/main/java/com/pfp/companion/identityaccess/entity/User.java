package com.pfp.companion.identityaccess.entity;

import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public record User(UUID id, String email, String passwordHash, Role role, boolean emailVerified,
        Instant createdAt, List<OAuthIdentity> oauthIdentities) {

    public User {
        Objects.requireNonNull(id);
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("email must not be blank");
        }
        email = email.strip().toLowerCase(java.util.Locale.ROOT);
        Objects.requireNonNull(role);
        Objects.requireNonNull(createdAt);
        oauthIdentities = List.copyOf(oauthIdentities);
        if (role == Role.ROLE_GUEST) {
            throw new IllegalArgumentException("guest workflow must not be stored as user");
        }
        if ((passwordHash == null || passwordHash.isBlank()) && oauthIdentities.isEmpty()) {
            throw new IllegalArgumentException("user requires password hash or OAuth identity");
        }
    }

    public User verified() {
        return new User(id, email, passwordHash, role, true, createdAt, oauthIdentities);
    }

    public User withPasswordHash(String updatedPasswordHash) {
        return new User(id, email, updatedPasswordHash, role, emailVerified, createdAt,
                oauthIdentities);
    }

    public User withOAuthIdentity(OAuthIdentity identity) {
        Objects.requireNonNull(identity);
        if (oauthIdentities.contains(identity)) {
            return this;
        }
        List<OAuthIdentity> updatedIdentities = new java.util.ArrayList<>(oauthIdentities);
        updatedIdentities.add(identity);
        return new User(id, email, passwordHash, role, emailVerified, createdAt, updatedIdentities);
    }
}
