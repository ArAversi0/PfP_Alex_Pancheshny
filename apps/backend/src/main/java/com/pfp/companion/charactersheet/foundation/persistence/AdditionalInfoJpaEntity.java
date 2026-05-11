package com.pfp.companion.charactersheet.foundation.persistence;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "additional_info")
class AdditionalInfoJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @OneToOne(optional = false)
    @JoinColumn(name = "character_id", nullable = false, unique = true)
    CharacterJpaEntity character;

    String appearance;
    String detailedOrigin;
    String alliesAndOrganizations;
    String notesPrimary;
    String notesSecondary;

    protected AdditionalInfoJpaEntity() {
    }
}

