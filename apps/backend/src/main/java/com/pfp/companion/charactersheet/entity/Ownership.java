package com.pfp.companion.charactersheet.entity;

import java.util.Objects;
import java.util.UUID;

public record Ownership(Type type, UUID userId) {

    public enum Type {
        AUTHENTICATED,
        LOCAL_GUEST
    }

    public Ownership {
        Objects.requireNonNull(type, "type must not be null");
        if (type == Type.AUTHENTICATED && userId == null) {
            throw new IllegalArgumentException("authenticated ownership requires userId");
        }
        if (type == Type.LOCAL_GUEST && userId != null) {
            throw new IllegalArgumentException("local guest ownership must not have userId");
        }
    }

    public static Ownership authenticated(UUID userId) {
        return new Ownership(Type.AUTHENTICATED, Objects.requireNonNull(userId));
    }

    public static Ownership localGuest() {
        return new Ownership(Type.LOCAL_GUEST, null);
    }
}

