package com.pfp.companion.charactersheet.foundation.persistence;

import java.util.Optional;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

interface CharacterJpaRepository extends JpaRepository<CharacterJpaEntity, Long> {

    long countByUserPublicId(UUID publicId);

    Optional<CharacterJpaEntity> findByPublicId(UUID publicId);

    List<CharacterJpaEntity> findAllByUserPublicIdOrderByCreatedAtDesc(UUID publicId);

    @Query(value = """
            SELECT u.public_id AS "userId",
                   u.email AS "email",
                   c.public_id AS "characterId",
                   c.name AS "name",
                   c.image_url AS "imageUrl",
                   c.created_at AS "createdAt",
                   ci.level AS "level",
                   ci.class_name AS "className",
                   ci.specialization AS "specialization"
              FROM characters c
              JOIN users u ON u.id = c.user_id
              JOIN character_info ci ON ci.character_id = c.id
             ORDER BY u.email ASC, c.created_at DESC
            """, nativeQuery = true)
    List<AdminCharacterCardProjection> findAdminCharacterCards();

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(value = "DELETE FROM characters WHERE public_id = :publicId", nativeQuery = true)
    int deleteDirectByPublicId(@Param("publicId") UUID publicId);

    interface AdminCharacterCardProjection {
        UUID getUserId();

        String getEmail();

        UUID getCharacterId();

        String getName();

        String getImageUrl();

        java.time.Instant getCreatedAt();

        int getLevel();

        String getClassName();

        String getSpecialization();
    }
}
