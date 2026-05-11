package com.pfp.companion.charactersheet.foundation.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.util.UUID;

@Entity
@Table(name = "spells")
class SpellJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(name = "public_id", nullable = false, unique = true)
    UUID publicId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "character_id", nullable = false)
    CharacterJpaEntity character;

    @Column(name = "spell_name", nullable = false)
    String spellName;

    @Column(name = "image_url", nullable = false)
    String imageUrl;

    @Column(name = "spell_type", nullable = false)
    String spellType;

    @Column(name = "spell_class", nullable = false)
    String spellClass;

    @Column(nullable = false)
    String requirements;

    @Column(nullable = false)
    String description;

    protected SpellJpaEntity() {
    }
}

