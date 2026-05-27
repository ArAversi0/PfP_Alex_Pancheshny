import type {
  AdditionalInfo,
  Blessings,
  BodyHealth,
  EquipmentSlotCode,
  StatKey,
} from "./characterTypes";

export interface GuestItem {
  id: string;
  type: "ITEM" | "EQUIPMENT" | "TRADE";
  title: string;
  image: string;
  weight: number;
  description?: string;
  equipmentType?: string;
  sellPriceBase?: number;
}

export interface GuestInventorySlot {
  index: number;
  itemId: string | null;
}

export interface GuestSpell {
  id: string;
  name: string;
  type: string;
  class: string;
  image: string;
  requirements: string;
  description?: string;
}

export interface GuestCharacter {
  name: string;
  image: string;
  info: {
    level: number;
    origin: string;
    background: string;
    class: string;
    specialization: string;
  };
  stats: Record<StatKey, number>;
  skills: Array<{ stat: string; name: string; level: number }>;
  condition: {
    hp: BodyHealth;
    passiveDefense: number;
    movementSpeed: number;
    maxCarryWeight: number;
  };
  blessings: Blessings;
  inventory: {
    items: GuestItem[];
    slots: GuestInventorySlot[];
  };
  equipment: Partial<Record<EquipmentSlotCode, string | null>>;
  money: {
    amountBase: number;
    displayCurrency: string;
  };
  spells: GuestSpell[];
  additionalInfo: AdditionalInfo;
}

export interface GuestExportDocument {
  schemaVersion: "1.0";
  character: GuestCharacter;
}
