export const CHARACTER_LIMIT = 100;

export interface CharacterCard {
  id: string;
  name: string;
  level: number;
  className: string;
  specialization: string;
  imageUrl: string;
}

export interface CharacterCreated {
  id: string;
  name: string;
}

export interface CharacterInfo {
  level: number;
  origin: string;
  background: string;
  className: string;
  specialization: string;
}

export interface CharacterInfoUpdate extends CharacterInfo {
  name: string;
}

export interface Stat {
  level: number;
  roll: string;
}

export type StatGroup =
  | "STRENGTH"
  | "DEXTERITY"
  | "STAMINA"
  | "INTELLIGENCE"
  | "CHARISMA"
  | "LUCK"
  | "MIND";

export type StatKey =
  | "strength"
  | "dexterity"
  | "stamina"
  | "intelligence"
  | "charisma"
  | "luck"
  | "mind";

export interface Skill {
  statGroup: StatGroup;
  skillName: string;
  level: number;
  skillRoll: string;
  effectiveRoll: string;
}

export interface BodyPartHealth {
  max: number;
  current: number;
}

export interface BodyHealth {
  head: BodyPartHealth;
  neck: BodyPartHealth;
  torso: BodyPartHealth;
  leftArm: BodyPartHealth;
  rightArm: BodyPartHealth;
  leftLeg: BodyPartHealth;
  rightLeg: BodyPartHealth;
}

export interface Condition {
  globalHealthPercent: number;
  passiveDefense: number;
  passiveDodge: number;
  movementSpeed: number;
  maxCarryWeight: number;
  currentCarryWeight: number;
  overweight: boolean;
  hp: BodyHealth;
}

export interface Blessings {
  blessings: number;
  inspirations: number;
}

export interface AdditionalInfo {
  appearance: string;
  detailedOrigin: string;
  allies: string;
  notesPrimary: string;
  notesSecondary: string;
}

export type EquipmentSlotCode =
  | "HEAD"
  | "NECK"
  | "TORSO"
  | "ARMS"
  | "LEGS"
  | "WEAPON_1"
  | "WEAPON_2"
  | "TALISMAN_1"
  | "TALISMAN_2"
  | "TALISMAN_3"
  | "TALISMAN_4";

export interface Item {
  id: string;
  type: "ITEM" | "EQUIPMENT" | "TRADE";
  title: string;
  imageUrl: string;
  weight: number;
  description: string;
  equipmentType: string | null;
  sellPriceBase: number | null;
}

export interface ItemUpsert {
  type: "ITEM" | "EQUIPMENT" | "TRADE";
  title: string;
  imageUrl: string;
  weight: number;
  description: string;
  equipmentType: string | null;
  sellPriceBase: number | null;
}

export interface ItemPlacement {
  slotIndex: number;
  item: Item;
}

export interface SellItemResult {
  deleted: boolean;
  money: Money;
}

export interface MoveInventoryItem {
  fromSlotIndex: number;
  toSlotIndex: number;
}

export interface InventorySlot {
  slotIndex: number;
  item: Item | null;
}

export interface Inventory {
  maxCarryWeight: number;
  currentCarryWeight: number;
  overweight: boolean;
  slots: InventorySlot[];
}

export interface EquippedItem {
  itemId: string;
  title: string;
  imageUrl: string;
}

export type Equipment = Record<EquipmentSlotCode, EquippedItem | null>;

export interface Money {
  amountBase: number;
  displayAmount: number;
  displayCurrency: string;
}

export interface Spell {
  id: string;
  name: string;
  type: string;
  spellClass: string;
  imageUrl: string;
  requirements: string;
  description: string;
}

export interface SpellUpsert {
  name: string;
  type: string;
  spellClass: string;
  imageUrl: string;
  requirements: string;
  description: string;
}

export interface CharacterSheet {
  id: string;
  name: string;
  imageUrl: string;
  info: CharacterInfo;
  stats: Record<StatKey, Stat>;
  skills: Skill[];
  condition: Condition;
  blessings: Blessings;
  additionalInfo: AdditionalInfo;
  inventory: Inventory;
  equipment: Equipment;
  money: Money;
  spells: Spell[];
}
