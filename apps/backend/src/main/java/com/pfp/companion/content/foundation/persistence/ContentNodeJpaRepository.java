package com.pfp.companion.content.foundation.persistence;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

interface ContentNodeJpaRepository extends JpaRepository<ContentNodeJpaEntity, Long> {

    List<ContentNodeJpaEntity> findAllBySectionAndPublishedTrueOrderBySortOrderAscSlugAsc(String section);

    Optional<ContentNodeJpaEntity> findBySectionAndSlugAndPublishedTrue(String section, String slug);

    List<ContentNodeJpaEntity> findAllBySectionOrderBySortOrderAscSlugAsc(String section);

    Optional<ContentNodeJpaEntity> findBySectionAndSlug(String section, String slug);

    boolean existsBySectionAndParentSlug(String section, String parentSlug);

    void deleteBySectionAndSlug(String section, String slug);
}
