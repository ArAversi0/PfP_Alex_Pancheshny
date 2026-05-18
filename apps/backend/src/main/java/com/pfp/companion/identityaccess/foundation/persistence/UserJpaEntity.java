package com.pfp.companion.identityaccess.foundation.persistence;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "users")
class UserJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(name = "public_id", nullable = false, unique = true)
    UUID publicId;

    @Column(nullable = false, unique = true)
    String email;

    @Column(name = "password_hash")
    String passwordHash;

    @Column(nullable = false)
    String role;

    @Column(name = "email_verified", nullable = false)
    boolean emailVerified;

    @Column(name = "created_at", nullable = false)
    Instant createdAt;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    List<OAuthIdentityJpaEntity> oauthIdentities = new ArrayList<>();

    protected UserJpaEntity() {
    }
}
