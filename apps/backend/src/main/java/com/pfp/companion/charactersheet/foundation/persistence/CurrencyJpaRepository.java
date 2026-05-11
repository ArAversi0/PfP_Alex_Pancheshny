package com.pfp.companion.charactersheet.foundation.persistence;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

interface CurrencyJpaRepository extends JpaRepository<CurrencyJpaEntity, Long> {

    Optional<CurrencyJpaEntity> findByCode(String code);
}

