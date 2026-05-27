import type { EquipmentSlotCode, StatGroup, StatKey } from "./characterTypes";

export const STAT_DEFINITIONS: Array<{ key: StatKey; group: StatGroup; title: string }> = [
  { key: "strength", group: "STRENGTH", title: "Strength" },
  { key: "dexterity", group: "DEXTERITY", title: "Dexterity" },
  { key: "stamina", group: "STAMINA", title: "Stamina" },
  { key: "intelligence", group: "INTELLIGENCE", title: "Intelligence" },
  { key: "charisma", group: "CHARISMA", title: "Charisma" },
  { key: "luck", group: "LUCK", title: "Luck" },
  { key: "mind", group: "MIND", title: "Mind" },
];

export const SKILL_DEFINITIONS: Array<{ statGroup: StatGroup; skillName: string }> = [
  ["STRENGTH", "ATHLETICS"], ["STRENGTH", "BLOCKING"], ["STRENGTH", "GRAPPLING"],
  ["STRENGTH", "BRUTE_FORCE"], ["DEXTERITY", "REFLEXES"], ["DEXTERITY", "EVASION"],
  ["DEXTERITY", "ACROBATICS"], ["DEXTERITY", "STEALTH"], ["DEXTERITY", "SLEIGHT_OF_HAND"],
  ["DEXTERITY", "BALANCE"], ["STAMINA", "PAIN_TOLERANCE"], ["STAMINA", "ENDURANCE"],
  ["STAMINA", "CARRYING_CAPACITY"], ["INTELLIGENCE", "ANALYSIS"], ["INTELLIGENCE", "MAGIC"],
  ["INTELLIGENCE", "RELIGION"], ["INTELLIGENCE", "SURVIVAL"], ["INTELLIGENCE", "MEDICINE"],
  ["INTELLIGENCE", "SCIENCE"], ["INTELLIGENCE", "AWARENESS"], ["CHARISMA", "RHETORIC"],
  ["CHARISMA", "PERFORMANCE"], ["CHARISMA", "INTIMIDATION"], ["CHARISMA", "CHARM"],
  ["CHARISMA", "BUSINESS_SENSE"], ["LUCK", "FORTUNE"], ["LUCK", "PROFIT"],
  ["MIND", "WILLPOWER"], ["MIND", "COMPOSURE"], ["MIND", "SUGGESTION"],
].map(([statGroup, skillName]) => ({ statGroup: statGroup as StatGroup, skillName }));

export const EQUIPMENT_GROUPS: Array<{ title: string; slots: EquipmentSlotCode[] }> = [
  { title: "Armor", slots: ["HEAD", "NECK", "TORSO", "ARMS", "LEGS"] },
  { title: "Weapons", slots: ["WEAPON_1", "WEAPON_2"] },
  { title: "Trinkets", slots: ["TALISMAN_1", "TALISMAN_2", "TALISMAN_3", "TALISMAN_4"] },
];

export const EQUIPMENT_SLOTS = EQUIPMENT_GROUPS.flatMap(({ slots }) => slots);

export const EQUIPMENT_TYPE_OPTIONS = ["HEAD", "NECK", "TORSO", "ARMS", "LEGS", "WEAPON", "TALISMAN"] as const;
export const SPELL_TYPE_OPTIONS = ["SPELL", "CANTRIP", "RITUAL"] as const;
export const SPELL_CLASS_OPTIONS = ["PRIEST", "SPELLCASTER", "WARLOCK", "DRUID", "ARTIST", "INQUISITOR", "SAVAGE"] as const;

export function slotsForEquipmentType(equipmentType: string | null): EquipmentSlotCode[] {
  if (equipmentType === "WEAPON") return ["WEAPON_1", "WEAPON_2"];
  if (equipmentType === "TALISMAN") return ["TALISMAN_1", "TALISMAN_2", "TALISMAN_3", "TALISMAN_4"];
  if (EQUIPMENT_SLOTS.includes(equipmentType as EquipmentSlotCode)) return [equipmentType as EquipmentSlotCode];
  return [];
}

export function label(value: string): string {
  return value.replace(/^TALISMAN/, "TRINKET")
    .replace(/([a-z])([A-Z])/g, "$1 $2").replace(/_/g, " ").toLowerCase()
    .replace(/\b\w/g, (character) => character.toUpperCase());
}
