package com.pfp.companion.content.foundation.persistence;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

interface ContentEditorJpaRepository extends JpaRepository<ContentEditorJpaEntity, Long> {

    Optional<ContentEditorJpaEntity> findByPublicId(UUID publicId);
}

