package com.pfp.companion.charactersheet.foundation.persistence;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "character_info")
class CharacterInfoJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @OneToOne(optional = false)
    @JoinColumn(name = "character_id", nullable = false, unique = true)
    CharacterJpaEntity character;

    int level;
    String origin;
    String background;
    String className;
    String specialization;

    protected CharacterInfoJpaEntity() {
    }
}

