import { useRef, useState } from "react";
import { Link } from "react-router-dom";
import { FormMessage } from "../../auth/components/FormMessage";
import { SiteLayout } from "../../../shared/ui/SiteLayout";
import { CharacterSheetView } from "../components/CharacterSheetView";
import { downloadJson } from "../model/downloadJson";
import {
  addGuestInventoryRow,
  addGuestSpell,
  addGuestItem,
  applySheetEditsToGuest,
  deleteGuestSpell,
  deriveGuestSheet,
  equipGuestItem,
  moveGuestInventoryItem,
  parseGuestDocument,
  removeGuestInventoryRow,
  serializeGuestDocument,
  sellGuestTradeItem,
  throwAwayGuestItem,
  unequipGuestItem,
  updateGuestSpell,
  updateGuestItem,
} from "../model/pfpGuestRules";
import {
  clearGuestCharacter,
  loadGuestCharacter,
  saveGuestCharacter,
} from "../model/guestCharacterStorage";
import type { CharacterSheet, EquipmentSlotCode, ItemUpsert, SpellUpsert } from "../model/characterTypes";
import type { GuestCharacter } from "../model/guestCharacterTypes";

export function GuestCharacterPage() {
  const [character, setCharacter] = useState(loadGuestCharacter);
  const [message, setMessage] = useState("");
  const [error, setError] = useState("");
  const importInput = useRef<HTMLInputElement>(null);
  const sheet = deriveGuestSheet(character);

  function persist(updated: GuestCharacter) {
    saveGuestCharacter(updated);
    setCharacter(updated);
  }

  function persistUpdate(transform: (current: GuestCharacter) => GuestCharacter) {
    setCharacter((current) => {
      const updated = transform(current);
      saveGuestCharacter(updated);
      return updated;
    });
  }

  async function save(updatedSheet: CharacterSheet) {
    setError("");
    persistUpdate((current) => applySheetEditsToGuest(current, updatedSheet));
    setMessage("Local guest sheet updated.");
  }

  async function addInventoryRow() {
    setError("");
    persist(addGuestInventoryRow(character));
    setMessage("Added ten local inventory slots.");
  }

  async function removeInventoryRow() {
    setError("");
    persist(removeGuestInventoryRow(character));
    setMessage("Removed the last empty inventory row.");
  }

  async function importCharacter(file?: File) {
    if (!file) return;
    setError("");
    setMessage("");
    try {
      persist(parseGuestDocument(await file.text()));
      setMessage("Imported character JSON into this guest session.");
    } catch (importError) {
      setError(importError instanceof Error ? importError.message : "Could not import character JSON.");
    } finally {
      if (importInput.current) importInput.current.value = "";
    }
  }

  async function exportCharacter() {
    downloadJson(character.name, serializeGuestDocument(character));
    setMessage("Exported guest character JSON.");
  }

  async function moveInventoryItem(fromSlotIndex: number, toSlotIndex: number) {
    setError("");
    persistUpdate((current) => moveGuestInventoryItem(current, fromSlotIndex, toSlotIndex));
  }

  async function createItem(item: ItemUpsert) {
    setError("");
    const updated = addGuestItem(character, item);
    persist(updated);
    setMessage(`Added ${item.title}.`);
    return deriveGuestSheet(updated);
  }

  async function updateItem(itemId: string, item: ItemUpsert) {
    setError("");
    persistUpdate((current) => updateGuestItem(current, itemId, item));
    setMessage(`Updated ${item.title}.`);
  }

  async function throwAwayItem(itemId: string) {
    setError("");
    persist(throwAwayGuestItem(character, itemId));
    setMessage("Item thrown away.");
  }

  async function sellItem(itemId: string) {
    setError("");
    persist(sellGuestTradeItem(character, itemId));
    setMessage("Trade item sold.");
  }

  async function equipItem(itemId: string, slotCode: EquipmentSlotCode) {
    setError("");
    persist(equipGuestItem(character, itemId, slotCode));
    setMessage(`Equipped item to ${slotCode.replace("_", " ").toLowerCase()}.`);
  }

  async function unequipItem(slotCode: EquipmentSlotCode) {
    setError("");
    persist(unequipGuestItem(character, slotCode));
    setMessage(`Unequipped ${slotCode.replace("_", " ").toLowerCase()}.`);
  }

  async function createSpell(spell: SpellUpsert) {
    setError("");
    persistUpdate((current) => addGuestSpell(current, spell));
    setMessage(`Added ${spell.name}.`);
  }

  async function updateSpell(spellId: string, spell: SpellUpsert) {
    setError("");
    persistUpdate((current) => updateGuestSpell(current, spellId, spell));
    setMessage(`Updated ${spell.name}.`);
  }

  async function deleteSpell(spellId: string) {
    setError("");
    persistUpdate((current) => deleteGuestSpell(current, spellId));
    setMessage("Spell deleted.");
  }

  function reset() {
    if (!window.confirm("Clear this local guest sheet and start over?")) return;
    setCharacter(clearGuestCharacter());
    setError("");
    setMessage("Guest sheet reset.");
  }

  return (
    <SiteLayout
      wide
      actions={<Link className="button ghost compact-button" to="/login">Sign in</Link>}
    >
      <section className="guest-toolbar">
        <div>
          <p className="eyebrow">No account required</p>
          <h2>Local character workspace</h2>
        </div>
        <div className="sheet-actions">
          <input
            ref={importInput}
            className="visually-hidden"
            type="file"
            accept="application/json,.json"
            onChange={(event) => void importCharacter(event.target.files?.[0])}
          />
          <button className="button ghost" onClick={() => importInput.current?.click()}>Import JSON</button>
          <button className="button ghost" onClick={reset}>Reset guest sheet</button>
        </div>
      </section>
      {message && <FormMessage kind="success">{message}</FormMessage>}
      {error && <FormMessage kind="error">{error}</FormMessage>}
      <CharacterSheetView
        sheet={sheet}
        mode="guest"
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
