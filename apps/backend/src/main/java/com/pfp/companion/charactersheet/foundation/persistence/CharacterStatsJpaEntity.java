package com.pfp.companion.charactersheet.foundation.persistence;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "character_stats")
class CharacterStatsJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @OneToOne(optional = false)
    @JoinColumn(name = "character_id", nullable = false, unique = true)
    CharacterJpaEntity character;

    int strength;
    int dexterity;
    int stamina;
    int intelligence;
    int charisma;
    int luck;
    int mind;

    @OneToMany(mappedBy = "stats", cascade = CascadeType.ALL, orphanRemoval = true)
    List<CharacterSkillJpaEntity> skills = new ArrayList<>();

    protected CharacterStatsJpaEntity() {
    }
}

