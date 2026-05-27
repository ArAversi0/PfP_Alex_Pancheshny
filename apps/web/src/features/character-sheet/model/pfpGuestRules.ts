import { EQUIPMENT_SLOTS, SKILL_DEFINITIONS, STAT_DEFINITIONS } from "./characterDefinitions";
import type {
  BodyHealth,
  CharacterSheet,
  Equipment,
  EquipmentSlotCode,
  ItemUpsert,
  InventorySlot,
  SpellUpsert,
  StatGroup,
} from "./characterTypes";
import type { GuestCharacter, GuestExportDocument, GuestItem } from "./guestCharacterTypes";

const DIE_SIDES = [4, 6, 8, 10, 12];
export const CURRENCY_CODES = ["CURRENCY_1", "CURRENCY_2", "CURRENCY_3", "CURRENCY_4"] as const;

const CURRENCY_RATES: Record<string, number> = {
  CURRENCY_1: 1,
  CURRENCY_2: 10,
  CURRENCY_3: 100,
  CURRENCY_4: 1000,
};

// Keep aligned with libs/game-rules ProgressionCalculator.
export function rollForLevel(level: number): string {
  requireNonNegative(level, "level");
  if (level === 0) return "3";
  const cycleIndex = (level - 1) % DIE_SIDES.length;
  const bonus = Math.floor((level - 1) / DIE_SIDES.length) * 13;
  return `3d${DIE_SIDES[cycleIndex]}${bonus ? `+${bonus}` : ""}`;
}

export function effectiveSkillRoll(statLevel: number, skillLevel: number): string {
  return rollForLevel(Math.floor((statLevel + skillLevel) / 2));
}

export function passiveDodge(dexterityLevel: number): number {
  requireNonNegative(dexterityLevel, "dexterityLevel");
  if (dexterityLevel === 0) return 3;
  return Math.floor((dexterityLevel - 1) / DIE_SIDES.length) * 12
    + DIE_SIDES[(dexterityLevel - 1) % DIE_SIDES.length];
}

// Keep aligned with libs/game-rules HealthCalculator.
export function globalHealthPercent(hp: BodyHealth): number {
  const limbKeys = new Set(["leftArm", "rightArm", "leftLeg", "rightLeg"]);
  const damage = Object.entries(hp).reduce((total, [part, health]) => {
    if (health.max === 0) return total;
    const denominator = limbKeys.has(part) ? health.max * 3 : health.max;
    return total + ((health.max - health.current) * 100) / denominator;
  }, 0);
  return round2(Math.max(0, 100 - damage));
}

export function deriveGuestSheet(character: GuestCharacter): CharacterSheet {
  const currentCarryWeight = character.inventory.items.reduce((sum, item) => sum + item.weight, 0);
  const stats = Object.fromEntries(STAT_DEFINITIONS.map(({ key }) => [
    key,
    { level: character.stats[key], roll: rollForLevel(character.stats[key]) },
  ])) as CharacterSheet["stats"];
  const statByGroup = Object.fromEntries(STAT_DEFINITIONS.map(({ key, group }) => [
    group,
    character.stats[key],
  ])) as Record<StatGroup, number>;
  const itemsById = new Map(character.inventory.items.map((item) => [item.id, item]));
  const equipment = Object.fromEntries(EQUIPMENT_SLOTS.map((slot) => {
    const itemId = character.equipment[slot];
    const item = itemId ? itemsById.get(itemId) : undefined;
    return [slot, item ? { itemId: item.id, title: item.title, imageUrl: item.image } : null];
  })) as Equipment;
  const slots: InventorySlot[] = character.inventory.slots.map(({ index, itemId }) => {
    const item = itemId ? itemsById.get(itemId) : undefined;
    return {
      slotIndex: index,
      item: item ? {
        id: item.id, type: item.type, title: item.title, imageUrl: item.image,
        weight: item.weight, description: item.description ?? "",
        equipmentType: item.equipmentType ?? null, sellPriceBase: item.sellPriceBase ?? null,
      } : null,
    };
  });
  return {
    id: "local-guest",
    name: character.name,
    imageUrl: character.image,
    info: { ...character.info, className: character.info.class },
    stats,
    skills: character.skills.map((skill) => ({
      statGroup: skill.stat as StatGroup,
      skillName: skill.name,
      level: skill.level,
      skillRoll: rollForLevel(skill.level),
      effectiveRoll: effectiveSkillRoll(statByGroup[skill.stat as StatGroup], skill.level),
    })),
    condition: {
      ...character.condition,
      globalHealthPercent: globalHealthPercent(character.condition.hp),
      passiveDodge: passiveDodge(character.stats.dexterity),
      currentCarryWeight,
      overweight: currentCarryWeight > character.condition.maxCarryWeight,
    },
    blessings: character.blessings,
    additionalInfo: character.additionalInfo,
    inventory: {
      maxCarryWeight: character.condition.maxCarryWeight,
      currentCarryWeight,
      overweight: currentCarryWeight > character.condition.maxCarryWeight,
      slots,
    },
    equipment,
    money: {
      amountBase: character.money.amountBase,
      displayAmount: displayAmountForCurrency(character.money.amountBase, character.money.displayCurrency),
      displayCurrency: character.money.displayCurrency,
    },
    spells: character.spells.map((spell) => ({
      id: spell.id, name: spell.name, type: spell.type, spellClass: spell.class,
      imageUrl: spell.image, requirements: spell.requirements, description: spell.description ?? "",
    })),
  };
}

export function displayAmountForCurrency(amountBase: number, displayCurrency: string): number {
  return Math.floor(amountBase / (CURRENCY_RATES[displayCurrency] ?? 1));
}

export function baseAmountFromDisplay(displayAmount: number, displayCurrency: string): number {
  return Math.round(displayAmount * (CURRENCY_RATES[displayCurrency] ?? 1) * 100) / 100;
}

export function createBlankGuestCharacter(): GuestCharacter {
  return {
    name: "Unnamed adventurer",
    image: "",
    info: { level: 1, origin: "", background: "", class: "", specialization: "" },
    stats: Object.fromEntries(STAT_DEFINITIONS.map(({ key }) => [key, 0])) as Record<typeof STAT_DEFINITIONS[number]["key"], number>,
    skills: SKILL_DEFINITIONS.map(({ statGroup, skillName }) => ({ stat: statGroup, name: skillName, level: 0 })),
    condition: {
      hp: {
        head: { max: 60, current: 60 }, neck: { max: 40, current: 40 },
        torso: { max: 100, current: 100 }, leftArm: { max: 60, current: 60 },
        rightArm: { max: 60, current: 60 }, leftLeg: { max: 60, current: 60 },
        rightLeg: { max: 60, current: 60 },
      },
      passiveDefense: 0, movementSpeed: 0, maxCarryWeight: 0,
    },
    blessings: { blessings: 0, inspirations: 0 },
    inventory: { items: [], slots: Array.from({ length: 10 }, (_, index) => ({ index, itemId: null })) },
    equipment: Object.fromEntries(EQUIPMENT_SLOTS.map((slot) => [slot, null])),
    money: { amountBase: 0, displayCurrency: "CURRENCY_1" },
    spells: [],
    additionalInfo: { appearance: "", detailedOrigin: "", allies: "", notesPrimary: "", notesSecondary: "" },
  };
}

export function applySheetEditsToGuest(
  character: GuestCharacter,
  sheet: CharacterSheet,
): GuestCharacter {
  return {
    ...character,
    name: sheet.name,
    image: sheet.imageUrl,
    info: {
      level: sheet.info.level,
      origin: sheet.info.origin,
      background: sheet.info.background,
      class: sheet.info.className,
      specialization: sheet.info.specialization,
    },
    stats: Object.fromEntries(STAT_DEFINITIONS.map(({ key }) => [
      key, sheet.stats[key].level,
    ])) as GuestCharacter["stats"],
    skills: sheet.skills.map((skill) => ({
      stat: skill.statGroup,
      name: skill.skillName,
      level: skill.level,
    })),
    condition: {
      hp: sheet.condition.hp,
      passiveDefense: sheet.condition.passiveDefense,
      movementSpeed: sheet.condition.movementSpeed,
      maxCarryWeight: sheet.condition.maxCarryWeight,
    },
    blessings: sheet.blessings,
    additionalInfo: sheet.additionalInfo,
    money: {
      amountBase: sheet.money.amountBase,
      displayCurrency: sheet.money.displayCurrency,
    },
  };
}

export function addGuestInventoryRow(character: GuestCharacter): GuestCharacter {
  const firstIndex = character.inventory.slots.reduce(
    (highest, slot) => Math.max(highest, slot.index + 1),
    0,
  );
  return {
    ...character,
    inventory: {
      ...character.inventory,
      slots: [
        ...character.inventory.slots,
        ...Array.from({ length: 10 }, (_, offset) => ({
          index: firstIndex + offset,
          itemId: null,
        })),
      ],
    },
  };
}

export function removeGuestInventoryRow(character: GuestCharacter): GuestCharacter {
  if (character.inventory.slots.length <= 10) return character;
  const sortedSlots = [...character.inventory.slots].sort((left, right) => left.index - right.index);
  const lastRow = sortedSlots.slice(-10);
  if (lastRow.some((slot) => slot.itemId)) return character;
  const lastRowIndexes = new Set(lastRow.map((slot) => slot.index));
  return {
    ...character,
    inventory: {
      ...character.inventory,
      slots: character.inventory.slots.filter((slot) => !lastRowIndexes.has(slot.index)),
    },
  };
}

export function moveGuestInventoryItem(
  character: GuestCharacter,
  fromSlotIndex: number,
  toSlotIndex: number,
): GuestCharacter {
  if (fromSlotIndex === toSlotIndex) return character;
  const from = character.inventory.slots.find((slot) => slot.index === fromSlotIndex);
  const to = character.inventory.slots.find((slot) => slot.index === toSlotIndex);
  if (!from || !to || !from.itemId) return character;
  return {
    ...character,
    inventory: {
      ...character.inventory,
      slots: character.inventory.slots.map((slot) => {
        if (slot.index === fromSlotIndex) return { ...slot, itemId: to.itemId };
        if (slot.index === toSlotIndex) return { ...slot, itemId: from.itemId };
        return slot;
      }),
    },
  };
}

export function addGuestItem(character: GuestCharacter, item: ItemUpsert): GuestCharacter {
  const nextItem: GuestItem = {
    id: randomId(),
    type: item.type,
    title: item.title,
    image: item.imageUrl,
    weight: item.weight,
    description: item.description,
    equipmentType: item.type === "EQUIPMENT" ? item.equipmentType ?? undefined : undefined,
    sellPriceBase: item.type === "TRADE" ? item.sellPriceBase ?? 0 : undefined,
  };
  const prepared = character.inventory.slots.some((slot) => !slot.itemId)
    ? character
    : addGuestInventoryRow(character);
  let placed = false;
  return {
    ...prepared,
    inventory: {
      items: [...prepared.inventory.items, nextItem],
      slots: prepared.inventory.slots.map((slot) => {
        if (placed || slot.itemId) return slot;
        placed = true;
        return { ...slot, itemId: nextItem.id };
      }),
    },
  };
}

export function updateGuestItem(
  character: GuestCharacter,
  itemId: string,
  updates: Pick<ItemUpsert, "title" | "description" | "weight" | "sellPriceBase">,
): GuestCharacter {
  return {
    ...character,
    inventory: {
      ...character.inventory,
      items: character.inventory.items.map((item) => item.id === itemId
        ? {
          ...item,
          title: updates.title,
          description: updates.description,
          weight: updates.weight,
          sellPriceBase: item.type === "TRADE" ? updates.sellPriceBase ?? 0 : item.sellPriceBase,
        }
        : item),
    },
  };
}

export function throwAwayGuestItem(character: GuestCharacter, itemId: string): GuestCharacter {
  return {
    ...character,
    inventory: {
      items: character.inventory.items.filter((item) => item.id !== itemId),
      slots: character.inventory.slots.map((slot) => slot.itemId === itemId
        ? { ...slot, itemId: null }
        : slot),
    },
    equipment: Object.fromEntries(EQUIPMENT_SLOTS.map((slot) => [
      slot,
      character.equipment[slot] === itemId ? null : character.equipment[slot] ?? null,
    ])),
  };
}

export function sellGuestTradeItem(character: GuestCharacter, itemId: string): GuestCharacter {
  const item = character.inventory.items.find((candidate) => candidate.id === itemId);
  if (!item || item.type !== "TRADE") return character;
  return {
    ...throwAwayGuestItem(character, itemId),
    money: {
      ...character.money,
      amountBase: character.money.amountBase + (item.sellPriceBase ?? 0),
    },
  };
}

export function equipGuestItem(
  character: GuestCharacter,
  itemId: string,
  slotCode: EquipmentSlotCode,
): GuestCharacter {
  return {
    ...character,
    equipment: {
      ...Object.fromEntries(EQUIPMENT_SLOTS.map((slot) => [
        slot,
        character.equipment[slot] === itemId ? null : character.equipment[slot] ?? null,
      ])),
      [slotCode]: itemId,
    },
  };
}

export function unequipGuestItem(
  character: GuestCharacter,
  slotCode: EquipmentSlotCode,
): GuestCharacter {
  return {
    ...character,
    equipment: { ...character.equipment, [slotCode]: null },
  };
}

export function addGuestSpell(character: GuestCharacter, spell: SpellUpsert): GuestCharacter {
  return {
    ...character,
    spells: [...character.spells, {
      id: randomId(),
      name: spell.name,
      type: spell.type,
      class: spell.spellClass,
      image: spell.imageUrl,
      requirements: spell.requirements,
      description: spell.description,
    }],
  };
}

export function updateGuestSpell(
  character: GuestCharacter,
  spellId: string,
  spell: SpellUpsert,
): GuestCharacter {
  return {
    ...character,
    spells: character.spells.map((candidate) => candidate.id === spellId
      ? {
        ...candidate,
        name: spell.name,
        type: spell.type,
        class: spell.spellClass,
        image: spell.imageUrl,
        requirements: spell.requirements,
        description: spell.description,
      }
      : candidate),
  };
}

export function deleteGuestSpell(character: GuestCharacter, spellId: string): GuestCharacter {
  return {
    ...character,
    spells: character.spells.filter((spell) => spell.id !== spellId),
  };
}

export function parseGuestDocument(json: string): GuestCharacter {
  const document = JSON.parse(json) as Partial<GuestExportDocument>;
  if (document.schemaVersion !== "1.0" || !document.character) {
    throw new Error("Only PfP character JSON schema version 1.0 is supported.");
  }
  return document.character;
}

export function serializeGuestDocument(character: GuestCharacter): string {
  return JSON.stringify({ schemaVersion: "1.0", character }, null, 2);
}

function requireNonNegative(value: number, field: string) {
  if (!Number.isFinite(value) || value < 0) throw new Error(`${field} must be non-negative`);
}

function round2(value: number): number {
  return Math.round(value * 100) / 100;
}

function randomId(): string {
  return window.crypto?.randomUUID?.() ?? `guest-${Date.now()}-${Math.random().toString(16).slice(2)}`;
}
