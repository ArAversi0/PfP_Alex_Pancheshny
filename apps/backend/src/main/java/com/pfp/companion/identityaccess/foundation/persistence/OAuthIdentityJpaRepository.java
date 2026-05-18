package com.pfp.companion.identityaccess.foundation.persistence;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

interface OAuthIdentityJpaRepository extends JpaRepository<OAuthIdentityJpaEntity, Long> {

    @Query("""
            SELECT identity
            FROM OAuthIdentityJpaEntity identity
            JOIN FETCH identity.user
            WHERE identity.provider = :provider
              AND identity.providerSubject = :providerSubject
            """)
    Optional<OAuthIdentityJpaEntity> findByProviderAndProviderSubject(
            @Param("provider") String provider,
            @Param("providerSubject") String providerSubject);
}
