package com.pfp.companion.content.foundation.persistence;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

interface RuleBookArticleJpaRepository extends JpaRepository<RuleBookArticleJpaEntity, Long> {

    List<RuleBookArticleJpaEntity> findAllByCategoryIdOrderById(long categoryId);
}

