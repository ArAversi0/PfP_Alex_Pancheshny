import { useEffect, useState } from "react";
import { Link, useParams } from "react-router-dom";
import { getApiErrorMessage } from "../../auth/api/authApi";
import { FormMessage } from "../../auth/components/FormMessage";
import { SiteLayout } from "../../../shared/ui/SiteLayout";
import { characterApi } from "../api/characterApi";
import { CharacterSheetView } from "../components/CharacterSheetView";
import { downloadJson } from "../model/downloadJson";
import type { CharacterSheet, EquipmentSlotCode, ItemUpsert, SpellUpsert } from "../model/characterTypes";

export function CharacterSheetPage() {
  const { characterId = "" } = useParams();
  const [sheet, setSheet] = useState<CharacterSheet | null>(null);
  const [error, setError] = useState("");
  const [saving, setSaving] = useState(false);

  useEffect(() => {
    characterApi.getSheet(characterId).then(
      (loaded) => setSheet(loaded),
      (loadError: unknown) => setError(getApiErrorMessage(loadError)),
    );
  }, [characterId]);

  async function refreshSheet(id = characterId) {
    setSheet(await characterApi.getSheet(id));
  }

  async function save(updated: CharacterSheet) {
    if (!sheet) return;
    setSaving(true);
    setError("");
    try {
      let changed = false;
      if (updated.name !== sheet.name || changedValue(updated.info, sheet.info)) {
        await characterApi.updateInfo(updated.id, { name: updated.name, ...updated.info });
        changed = true;
      }
      if (updated.imageUrl !== sheet.imageUrl) {
        await characterApi.updatePortrait(updated.id, updated.imageUrl);
        changed = true;
      }
      if (changedStatLevels(updated, sheet)) {
        await characterApi.updateStats(updated.id, Object.fromEntries(
          Object.entries(updated.stats).map(([key, stat]) => [key, stat.level]),
        ) as Record<keyof CharacterSheet["stats"], number>);
        changed = true;
      }
      if (changedSkillLevels(updated, sheet)) {
        await characterApi.updateSkills(updated.id, updated.skills);
        changed = true;
      }
      if (changedValue(updated.condition, sheet.condition)) {
        await characterApi.updateCondition(updated.id, updated.condition);
        changed = true;
      }
      if (changedValue(updated.blessings, sheet.blessings)) {
        await characterApi.updateBlessings(updated.id, updated.blessings);
        changed = true;
      }
      if (changedValue(updated.additionalInfo, sheet.additionalInfo)) {
        await characterApi.updateAdditionalInfo(updated.id, updated.additionalInfo);
        changed = true;
      }
      if (updated.money.amountBase !== sheet.money.amountBase) {
        await characterApi.setMoneyAmountBase(updated.id, updated.money.amountBase);
        changed = true;
      }
      if (updated.money.displayCurrency !== sheet.money.displayCurrency) {
        await characterApi.selectCurrency(updated.id, updated.money.displayCurrency);
        changed = true;
      }
      if (changed) {
        await refreshSheet(updated.id);
      } else {
        setSheet(updated);
      }
    } catch (saveError) {
      setError(getApiErrorMessage(saveError));
      throw saveError;
    } finally {
      setSaving(false);
    }
  }

  async function addInventoryRow() {
    if (!sheet) return;
    setError("");
    try {
      const inventory = await characterApi.addInventoryRow(sheet.id);
      setSheet({ ...sheet, inventory });
    } catch (rowError) {
      setError(getApiErrorMessage(rowError));
      throw rowError;
    }
  }

  async function removeInventoryRow() {
    if (!sheet) return;
    setError("");
    try {
      const inventory = await characterApi.removeInventoryRow(sheet.id);
      setSheet({ ...sheet, inventory });
    } catch (rowError) {
      setError(getApiErrorMessage(rowError));
      throw rowError;
    }
  }

  async function exportCharacter() {
    if (!sheet) return;
    setError("");
    try {
      downloadJson(sheet.name, await characterApi.exportJson(sheet.id));
    } catch (exportError) {
      setError(getApiErrorMessage(exportError));
    }
  }

  async function moveInventoryItem(fromSlotIndex: number, toSlotIndex: number) {
    if (!sheet) return;
    setError("");
    try {
      const inventory = await characterApi.moveInventoryItem(sheet.id, { fromSlotIndex, toSlotIndex });
      setSheet({ ...sheet, inventory });
    } catch (moveError) {
      setError(getApiErrorMessage(moveError));
      throw moveError;
    }
  }

  async function createItem(item: ItemUpsert) {
    if (!sheet) return;
    setError("");
    try {
      const updated = await characterApi.createItem(sheet.id, item);
      setSheet(updated);
      return updated;
    } catch (itemError) {
      setError(getApiErrorMessage(itemError));
      throw itemError;
    }
  }

  async function updateItem(itemId: string, item: ItemUpsert) {
    if (!sheet) return;
    setError("");
    try {
      const updated = await characterApi.updateItem(sheet.id, itemId, item);
      setSheet(updated);
      return updated;
    } catch (itemError) {
      setError(getApiErrorMessage(itemError));
      throw itemError;
    }
  }

  async function throwAwayItem(itemId: string) {
    if (!sheet) return;
    setError("");
    try {
      const updated = await characterApi.throwAwayItem(sheet.id, itemId);
      setSheet(updated);
      return updated;
    } catch (itemError) {
      setError(getApiErrorMessage(itemError));
      throw itemError;
    }
  }

  async function sellItem(itemId: string) {
    if (!sheet) return;
    setError("");
    try {
      const updated = await characterApi.sellTradeItem(sheet.id, itemId);
      setSheet(updated);
      return updated;
    } catch (itemError) {
      setError(getApiErrorMessage(itemError));
      throw itemError;
    }
  }

  async function equipItem(itemId: string, slotCode: EquipmentSlotCode) {
    if (!sheet) return;
    setError("");
    try {
      const updated = await characterApi.equipItem(sheet.id, itemId, slotCode);
      setSheet(updated);
      return updated;
    } catch (itemError) {
      setError(getApiErrorMessage(itemError));
      throw itemError;
    }
  }

  async function unequipItem(slotCode: EquipmentSlotCode) {
    if (!sheet) return;
    setError("");
    try {
      const updated = await characterApi.unequipItem(sheet.id, slotCode);
      setSheet(updated);
      return updated;
    } catch (itemError) {
      setError(getApiErrorMessage(itemError));
      throw itemError;
    }
  }

  async function createSpell(spell: SpellUpsert) {
    if (!sheet) return;
    setError("");
    try {
      const updated = await characterApi.createSpell(sheet.id, spell);
      setSheet(updated);
      return updated;
    } catch (spellError) {
      setError(getApiErrorMessage(spellError));
      throw spellError;
    }
  }

  async function updateSpell(spellId: string, spell: SpellUpsert) {
    if (!sheet) return;
    setError("");
    try {
      const updated = await characterApi.updateSpell(sheet.id, spellId, spell);
      setSheet(updated);
      return updated;
    } catch (spellError) {
      setError(getApiErrorMessage(spellError));
      throw spellError;
    }
  }

  async function deleteSpell(spellId: string) {
    if (!sheet) return;
    setError("");
    try {
      const updated = await characterApi.deleteSpell(sheet.id, spellId);
      setSheet(updated);
      return updated;
    } catch (spellError) {
      setError(getApiErrorMessage(spellError));
      throw spellError;
    }
  }

  if (!sheet) {
    return (
      <SiteLayout wide>
        {error
          ? <FormMessage kind="error">{error}</FormMessage>
          : <p className="loading-copy">Opening character sheet...</p>}
      </SiteLayout>
    );
  }

  return (
    <SiteLayout wide>
      {error && <FormMessage kind="error">{error}</FormMessage>}
      <div className="sheet-back-row">
        <Link className="button ghost compact-button" to="/characters">Back to archive</Link>
      </div>
      <CharacterSheetView
        sheet={sheet}
        mode="account"
        saving={saving}
        onSave={save}
        onAddInventoryRow={addInventoryRow}
        onRemoveInventoryRow={removeInventoryRow}
        onExport={exportCharacter}
        onCreateItem={createItem}
        onUpdateItem={updateItem}
        onThrowAwayItem={throwAwayItem}
        onSellItem={sellItem}
        onEquipItem={equipItem}
        onUnequipItem={unequipItem}
        onMoveInventoryItem={moveInventoryItem}
        onCreateSpell={createSpell}
        onUpdateSpell={updateSpell}
        onDeleteSpell={deleteSpell}
      />
    </SiteLayout>
  );
}

function changedValue(left: unknown, right: unknown): boolean {
  return JSON.stringify(left) !== JSON.stringify(right);
}

function changedStatLevels(updated: CharacterSheet, current: CharacterSheet): boolean {
  return Object.entries(updated.stats).some(([key, stat]) =>
    stat.level !== current.stats[key as keyof CharacterSheet["stats"]].level);
}

function changedSkillLevels(updated: CharacterSheet, current: CharacterSheet): boolean {
  if (updated.skills.length !== current.skills.length) {
    return true;
  }
  return updated.skills.some((skill, index) => {
    const currentSkill = current.skills[index];
    return !currentSkill
      || skill.skillName !== currentSkill.skillName
      || skill.statGroup !== currentSkill.statGroup
      || skill.level !== currentSkill.level;
  });
}
