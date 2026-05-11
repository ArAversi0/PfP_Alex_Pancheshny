package com.pfp.companion.charactersheet.foundation.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;

@Entity
@Table(name = "character_condition")
class CharacterConditionJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @OneToOne(optional = false)
    @JoinColumn(name = "character_id", nullable = false, unique = true)
    CharacterJpaEntity character;

    BigDecimal globalHealthPercent;
    int headMaxHp;
    int headCurrentHp;
    int neckMaxHp;
    int neckCurrentHp;
    int torsoMaxHp;
    int torsoCurrentHp;
    int leftArmMaxHp;
    int leftArmCurrentHp;
    int rightArmMaxHp;
    int rightArmCurrentHp;
    int leftLegMaxHp;
    int leftLegCurrentHp;
    int rightLegMaxHp;
    int rightLegCurrentHp;
    int passiveDefense;
    int passiveDodge;
    BigDecimal movementSpeed;
    BigDecimal maxCarryWeight;
    BigDecimal currentCarryWeight;

    protected CharacterConditionJpaEntity() {
    }
}

