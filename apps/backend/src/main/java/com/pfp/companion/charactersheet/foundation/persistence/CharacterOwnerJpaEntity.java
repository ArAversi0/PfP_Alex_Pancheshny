package com.pfp.companion.charactersheet.foundation.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;

@Entity
@Table(name = "users")
class CharacterOwnerJpaEntity {

    @Id
    Long id;

    @Column(name = "public_id", nullable = false, unique = true)
    UUID publicId;

    @Column(nullable = false)
    String email;

    UUID publicId() {
        return publicId;
    }

    protected CharacterOwnerJpaEntity() {
    }
}
