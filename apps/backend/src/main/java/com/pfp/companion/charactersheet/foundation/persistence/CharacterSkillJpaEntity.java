package com.pfp.companion.charactersheet.foundation.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "character_skills")
class CharacterSkillJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "stats_id", nullable = false)
    CharacterStatsJpaEntity stats;

    @Column(name = "stat_group", nullable = false)
    String statGroup;

    @Column(name = "skill_name", nullable = false)
    String skillName;

    @Column(name = "skill_level", nullable = false)
    int skillLevel;

    protected CharacterSkillJpaEntity() {
    }
}

