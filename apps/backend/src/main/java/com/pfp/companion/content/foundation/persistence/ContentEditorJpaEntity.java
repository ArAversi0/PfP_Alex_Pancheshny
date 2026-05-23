package com.pfp.companion.content.foundation.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;

@Entity
@Table(name = "users")
class ContentEditorJpaEntity {

    @Id
    Long id;

    @Column(name = "public_id", nullable = false, unique = true)
    UUID publicId;

    protected ContentEditorJpaEntity() {
    }
}

