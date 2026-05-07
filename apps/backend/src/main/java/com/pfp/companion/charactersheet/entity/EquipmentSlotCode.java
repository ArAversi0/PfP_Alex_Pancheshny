package com.pfp.companion.charactersheet.entity;

public enum EquipmentSlotCode {
    HEAD(EquipmentType.HEAD),
    NECK(EquipmentType.NECK),
    TORSO(EquipmentType.TORSO),
    ARMS(EquipmentType.ARMS),
    LEGS(EquipmentType.LEGS),
    WEAPON_1(EquipmentType.WEAPON),
    WEAPON_2(EquipmentType.WEAPON),
    TALISMAN_1(EquipmentType.TALISMAN),
    TALISMAN_2(EquipmentType.TALISMAN),
    TALISMAN_3(EquipmentType.TALISMAN),
    TALISMAN_4(EquipmentType.TALISMAN);

    private final EquipmentType acceptedType;

    EquipmentSlotCode(EquipmentType acceptedType) {
        this.acceptedType = acceptedType;
    }

    public EquipmentType acceptedType() {
        return acceptedType;
    }
}

