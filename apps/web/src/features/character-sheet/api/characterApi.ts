import { httpClient } from "../../../shared/api/httpClient";
import type {
  AdditionalInfo,
  Blessings,
  CharacterCard,
  CharacterCreated,
  CharacterInfoUpdate,
  CharacterSheet,
  Condition,
  EquipmentSlotCode,
  Inventory,
  ItemUpsert,
  Money,
  MoveInventoryItem,
  SpellUpsert,
  Skill,
  StatKey,
} from "../model/characterTypes";

const CHARACTERS_PATH = "/v1/characters";

export const characterApi = {
  async list() {
    return (await httpClient.get<CharacterCard[]>(CHARACTERS_PATH)).data;
  },

  async create(name: string) {
    return (await httpClient.post<CharacterCreated>(CHARACTERS_PATH, { name })).data;
  },

  async updateInfo(characterId: string, info: CharacterInfoUpdate) {
    return (
      await httpClient.put(`${CHARACTERS_PATH}/${characterId}/info`, info)
    ).data;
  },

  async updatePortrait(characterId: string, imageUrl: string) {
    return (
      await httpClient.put(`${CHARACTERS_PATH}/${characterId}/portrait`, { imageUrl })
    ).data;
  },

  async updateStats(characterId: string, stats: Record<StatKey, number>) {
    return (
      await httpClient.put(`${CHARACTERS_PATH}/${characterId}/stats`, stats)
    ).data;
  },

  async updateSkills(characterId: string, skills: Skill[]) {
    return (
      await httpClient.put(`${CHARACTERS_PATH}/${characterId}/skills`, skills.map((skill) => ({
        statGroup: skill.statGroup,
        skillName: skill.skillName,
        level: skill.level,
      })))
    ).data;
  },

  async updateCondition(characterId: string, condition: Condition) {
    const { passiveDefense, movementSpeed, maxCarryWeight, hp } = condition;
    return (
      await httpClient.put(`${CHARACTERS_PATH}/${characterId}/condition`, {
        passiveDefense, movementSpeed, maxCarryWeight, hp,
      })
    ).data;
  },

  async updateBlessings(characterId: string, blessings: Blessings) {
    return (
      await httpClient.put(`${CHARACTERS_PATH}/${characterId}/blessings`, blessings)
    ).data;
  },

  async updateAdditionalInfo(characterId: string, additionalInfo: AdditionalInfo) {
    return (
      await httpClient.put(`${CHARACTERS_PATH}/${characterId}/additional-info`, additionalInfo)
    ).data;
  },

  async remove(characterId: string) {
    await httpClient.delete(`${CHARACTERS_PATH}/${characterId}`);
  },

  async getSheet(characterId: string) {
    return (
      await httpClient.get<CharacterSheet>(`${CHARACTERS_PATH}/${characterId}/sheet`)
    ).data;
  },

  async importJson(document: string) {
    return (
      await httpClient.post<CharacterCreated>(`${CHARACTERS_PATH}/import`, document, {
        headers: { "Content-Type": "application/json" },
      })
    ).data;
  },

  async exportJson(characterId: string) {
    return (
      await httpClient.get<string>(`${CHARACTERS_PATH}/${characterId}/export`, {
        responseType: "text",
      })
    ).data;
  },

  async addInventoryRow(characterId: string) {
    return (
      await httpClient.post<Inventory>(`${CHARACTERS_PATH}/${characterId}/inventory/rows`, {
        rowsToAdd: 1,
      })
    ).data;
  },

  async removeInventoryRow(characterId: string) {
    return (
      await httpClient.delete<Inventory>(`${CHARACTERS_PATH}/${characterId}/inventory/rows`)
    ).data;
  },

  async createItem(characterId: string, item: ItemUpsert) {
    return (
      await httpClient.post<CharacterSheet>(`${CHARACTERS_PATH}/${characterId}/inventory/items`, item)
    ).data;
  },

  async updateItem(characterId: string, itemId: string, item: ItemUpsert) {
    return (
      await httpClient.put<CharacterSheet>(`${CHARACTERS_PATH}/${characterId}/inventory/items/${itemId}`, item)
    ).data;
  },

  async moveInventoryItem(characterId: string, move: MoveInventoryItem) {
    return (
      await httpClient.post<Inventory>(`${CHARACTERS_PATH}/${characterId}/inventory/slots/move`, move)
    ).data;
  },

  async throwAwayItem(characterId: string, itemId: string) {
    return (
      await httpClient.delete<CharacterSheet>(`${CHARACTERS_PATH}/${characterId}/inventory/items/${itemId}`)
    ).data;
  },

  async sellTradeItem(characterId: string, itemId: string) {
    return (
      await httpClient.post<CharacterSheet>(`${CHARACTERS_PATH}/${characterId}/inventory/items/${itemId}/sell`)
    ).data;
  },

  async equipItem(characterId: string, itemId: string, slotCode: EquipmentSlotCode) {
    return (
      await httpClient.post<CharacterSheet>(`${CHARACTERS_PATH}/${characterId}/equipment/equip`, {
        itemId,
        slotCode,
      })
    ).data;
  },

  async unequipItem(characterId: string, slotCode: EquipmentSlotCode) {
    return (
      await httpClient.post<CharacterSheet>(`${CHARACTERS_PATH}/${characterId}/equipment/unequip`, {
        slotCode,
      })
    ).data;
  },

  async selectCurrency(characterId: string, displayCurrency: string) {
    return (
      await httpClient.post<Money>(`${CHARACTERS_PATH}/${characterId}/money/currency`, {
        displayCurrency,
      })
    ).data;
  },

  async setMoneyAmountBase(characterId: string, amountBase: number) {
    return (
      await httpClient.put<Money>(`${CHARACTERS_PATH}/${characterId}/money`, {
        amountBase,
      })
    ).data;
  },

  async createSpell(characterId: string, spell: SpellUpsert) {
    return (
      await httpClient.post<CharacterSheet>(`${CHARACTERS_PATH}/${characterId}/spells`, spell)
    ).data;
  },

  async updateSpell(characterId: string, spellId: string, spell: SpellUpsert) {
    return (
      await httpClient.put<CharacterSheet>(`${CHARACTERS_PATH}/${characterId}/spells/${spellId}`, spell)
    ).data;
  },

  async deleteSpell(characterId: string, spellId: string) {
    return (
      await httpClient.delete<CharacterSheet>(`${CHARACTERS_PATH}/${characterId}/spells/${spellId}`)
    ).data;
  },
};
