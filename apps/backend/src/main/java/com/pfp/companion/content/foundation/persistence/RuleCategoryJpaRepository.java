package com.pfp.companion.content.foundation.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

interface RuleCategoryJpaRepository extends JpaRepository<RuleCategoryJpaEntity, Long> {

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(value = "DELETE FROM rule_categories WHERE id = :id", nativeQuery = true)
    int deleteDirectById(@Param("id") long id);
}
