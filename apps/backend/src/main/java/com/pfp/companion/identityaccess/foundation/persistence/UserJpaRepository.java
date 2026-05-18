package com.pfp.companion.identityaccess.foundation.persistence;

import java.util.Optional;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

interface UserJpaRepository extends JpaRepository<UserJpaEntity, Long> {

    Optional<UserJpaEntity> findByPublicId(UUID publicId);

    Optional<UserJpaEntity> findByEmail(String email);

    @Query(value = """
            SELECT u.public_id AS publicId,
                   u.email AS email,
                   u.role AS role,
                   u.email_verified AS emailVerified,
                   u.created_at AS createdAt,
                   COUNT(c.id) AS characterCount
              FROM users u
              LEFT JOIN characters c ON c.user_id = u.id
             GROUP BY u.id, u.public_id, u.email, u.role, u.email_verified, u.created_at
             ORDER BY u.created_at DESC
            """, nativeQuery = true)
    List<AdminUserSummaryProjection> findAdminUserSummaries();

    @Query(value = "SELECT COUNT(*) FROM characters", nativeQuery = true)
    long countCharacters();

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(value = "DELETE FROM users WHERE public_id = :publicId", nativeQuery = true)
    int deleteDirectByPublicId(@Param("publicId") UUID publicId);

    interface AdminUserSummaryProjection {
        UUID getPublicId();

        String getEmail();

        String getRole();

        boolean getEmailVerified();

        java.time.Instant getCreatedAt();

        long getCharacterCount();
    }
}
