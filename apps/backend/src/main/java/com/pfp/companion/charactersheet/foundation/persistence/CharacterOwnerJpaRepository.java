package com.pfp.companion.charactersheet.foundation.persistence;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

interface CharacterOwnerJpaRepository extends JpaRepository<CharacterOwnerJpaEntity, Long> {

    Optional<CharacterOwnerJpaEntity> findByPublicId(UUID publicId);
}

