import { useEffect, useState } from "react";
import blessingsBg from "../../../assets/sheet/blessings.png";
import defenceBg from "../../../assets/sheet/defence.png";
import dodgeBg from "../../../assets/sheet/dodge.png";
import healthBg from "../../../assets/sheet/health.png";
import inspirationsBg from "../../../assets/sheet/inspirations.png";
import movementBg from "../../../assets/sheet/movement.png";
import {
  EQUIPMENT_TYPE_OPTIONS,
  EQUIPMENT_GROUPS,
  SPELL_CLASS_OPTIONS,
  SPELL_TYPE_OPTIONS,
  STAT_DEFINITIONS,
  label,
  slotsForEquipmentType,
} from "../model/characterDefinitions";
import {
  baseAmountFromDisplay,
  CURRENCY_CODES,
  displayAmountForCurrency,
  effectiveSkillRoll,
  globalHealthPercent,
  passiveDodge,
  rollForLevel,
} from "../model/pfpGuestRules";
import type {
  BodyHealth,
  CharacterInfo,
  CharacterSheet,
  EquipmentSlotCode,
  Item,
  ItemUpsert,
  Skill,
  Spell,
  SpellUpsert,
  StatKey,
} from "../model/characterTypes";

interface CharacterSheetViewProps {
  sheet: CharacterSheet;
  mode: "account" | "guest";
  readOnly?: boolean;
  saving?: boolean;
  onSave: (sheet: CharacterSheet) => Promise<void>;
  onAddInventoryRow: () => Promise<void>;
  onRemoveInventoryRow: () => Promise<void>;
  onExport: () => Promise<void>;
  onCreateItem: (item: ItemUpsert) => Promise<CharacterSheet | void>;
  onUpdateItem: (itemId: string, item: ItemUpsert) => Promise<CharacterSheet | void>;
  onThrowAwayItem: (itemId: string) => Promise<CharacterSheet | void>;
  onSellItem: (itemId: string) => Promise<CharacterSheet | void>;
  onEquipItem: (itemId: string, slotCode: EquipmentSlotCode) => Promise<CharacterSheet | void>;
  onUnequipItem: (slotCode: EquipmentSlotCode) => Promise<CharacterSheet | void>;
  onMoveInventoryItem: (fromSlotIndex: number, toSlotIndex: number) => Promise<void>;
  onCreateSpell: (spell: SpellUpsert) => Promise<CharacterSheet | void>;
  onUpdateSpell: (spellId: string, spell: SpellUpsert) => Promise<CharacterSheet | void>;
  onDeleteSpell: (spellId: string) => Promise<CharacterSheet | void>;
}

const NARRATIVE_FIELDS = [
  ["appearance", "Appearance"],
  ["detailedOrigin", "Detailed origin"],
  ["allies", "Allies & organizations"],
  ["notesPrimary", "Primary notes"],
  ["notesSecondary", "Secondary notes"],
] as const;

const EMPTY_ITEM_FORM: ItemUpsert = {
  type: "ITEM",
  title: "",
  imageUrl: "",
  weight: 0,
  description: "",
  equipmentType: null,
  sellPriceBase: null,
};

const EMPTY_SPELL_FORM: SpellUpsert = {
  name: "",
  type: "SPELL",
  spellClass: "SPELLCASTER",
  imageUrl: "",
  requirements: "",
  description: "",
};

const METRIC_BACKGROUNDS = {
  defence: defenceBg,
  dodge: dodgeBg,
  blessings: blessingsBg,
  inspirations: inspirationsBg,
  movement: movementBg,
  health: healthBg,
} as const;

function moveItemInSheet(sheet: CharacterSheet, fromSlotIndex: number, toSlotIndex: number): CharacterSheet {
  const from = sheet.inventory.slots.find((slot) => slot.slotIndex === fromSlotIndex);
  const to = sheet.inventory.slots.find((slot) => slot.slotIndex === toSlotIndex);
  if (!from || !to) return sheet;
  return {
    ...sheet,
    inventory: {
      ...sheet.inventory,
      slots: sheet.inventory.slots.map((slot) => {
        if (slot.slotIndex === fromSlotIndex) return { ...slot, item: to.item };
        if (slot.slotIndex === toSlotIndex) return { ...slot, item: from.item };
        return slot;
      }),
    },
  };
}

export function CharacterSheetView({
  sheet,
  mode,
  readOnly = false,
  saving = false,
  onSave,
  onAddInventoryRow,
  onRemoveInventoryRow,
  onExport,
  onCreateItem,
  onUpdateItem,
  onThrowAwayItem,
  onSellItem,
  onEquipItem,
  onUnequipItem,
  onMoveInventoryItem,
  onCreateSpell,
  onUpdateSpell,
  onDeleteSpell,
}: CharacterSheetViewProps) {
  const [draft, setDraft] = useState(() => structuredClone(sheet));
  const [editing, setEditing] = useState(false);
  const [addingRow, setAddingRow] = useState(false);
  const [removingRow, setRemovingRow] = useState(false);
  const [itemFormOpen, setItemFormOpen] = useState(false);
  const [itemFormTargetSlot, setItemFormTargetSlot] = useState<number | null>(null);
  const [itemForm, setItemForm] = useState<ItemUpsert>(EMPTY_ITEM_FORM);
  const [itemEdits, setItemEdits] = useState<Record<string, ItemUpsert>>({});
  const [spellFormOpen, setSpellFormOpen] = useState(false);
  const [spellForm, setSpellForm] = useState<SpellUpsert>(EMPTY_SPELL_FORM);
  const [spellEdits, setSpellEdits] = useState<Record<string, SpellUpsert>>({});
  const [selectedSpellId, setSelectedSpellId] = useState<string | null>(null);
  const [selectedItemId, setSelectedItemId] = useState<string | null>(null);
  const [selectedEquipSlot, setSelectedEquipSlot] = useState<EquipmentSlotCode | "">("");
  const [itemBusy, setItemBusy] = useState(false);
  const [draggedSlotIndex, setDraggedSlotIndex] = useState<number | null>(null);

  useEffect(() => {
    if (!editing) setDraft(structuredClone(sheet));
  }, [editing, sheet]);

  useEffect(() => {
    if (editing) {
      setDraft((current) => ({
        ...current,
        inventory: sheet.inventory,
        equipment: sheet.equipment,
        spells: sheet.spells,
      }));
    }
  }, [editing, sheet.equipment, sheet.inventory, sheet.spells]);

  const inventoryWithItemEdits = editing ? {
    ...draft.inventory,
    slots: draft.inventory.slots.map((slot) => slot.item && itemEdits[slot.item.id]
      ? { ...slot, item: { ...slot.item, ...itemEdits[slot.item.id] } }
      : slot),
  } : sheet.inventory;
  const spellsWithEdits = editing
    ? draft.spells.map((spell) => spellEdits[spell.id]
      ? { ...spell, ...spellEdits[spell.id] }
      : spell)
    : sheet.spells;
  const shown = editing ? {
    ...draft,
    inventory: inventoryWithItemEdits,
    equipment: draft.equipment,
    spells: spellsWithEdits,
  } : sheet;
  const previewHealth = globalHealthPercent(shown.condition.hp);
  const previewDodge = passiveDodge(shown.stats.dexterity.level);
  const selectedItem = shown.inventory.slots.find((slot) => slot.item?.id === selectedItemId)?.item ?? null;
  const selectedSpell = shown.spells.find((spell) => spell.id === selectedSpellId) ?? null;
  const sortedInventorySlots = [...shown.inventory.slots]
    .sort((left, right) => left.slotIndex - right.slotIndex);
  const lastInventoryRow = sortedInventorySlots.slice(-10);
  const canRemoveInventoryRow = sortedInventorySlots.length > 10
    && lastInventoryRow.length === 10
    && lastInventoryRow.every((slot) => !slot.item);
  const currentCarryWeight = shown.inventory.currentCarryWeight;
  const maxCarryWeight = editing ? draft.condition.maxCarryWeight : shown.inventory.maxCarryWeight;
  const overweight = currentCarryWeight > maxCarryWeight;
  const hasInvalidItemEdit = Object.values(itemEdits).some((item) => !item.title.trim());
  const hasInvalidSpellEdit = Object.values(spellEdits).some((spell) =>
    !spell.name.trim() || !spell.requirements.trim());
  const canEdit = !readOnly;
  const canSaveChanges = canEdit && !saving && !!draft.name.trim() && !hasInvalidItemEdit && !hasInvalidSpellEdit;

  function normalizeItemUpdate(item: ItemUpsert): ItemUpsert {
    return {
      ...item,
      title: item.title.trim(),
      imageUrl: item.imageUrl.trim(),
    };
  }

  function clearItemEdit(itemId: string) {
    setItemEdits((current) => {
      if (!current[itemId]) return current;
      const next = { ...current };
      delete next[itemId];
      return next;
    });
  }

  async function save() {
    if (!canSaveChanges) return;
    try {
      await onSave({
        ...draft,
        name: draft.name.trim(),
        imageUrl: draft.imageUrl.trim(),
      });
      await Promise.all(Object.entries(itemEdits).map(([itemId, item]) =>
        onUpdateItem(itemId, normalizeItemUpdate(item))));
      await Promise.all(Object.entries(spellEdits).map(([spellId, spell]) => onUpdateSpell(spellId, {
        ...spell,
        name: spell.name.trim(),
        imageUrl: spell.imageUrl.trim(),
        requirements: spell.requirements.trim(),
      })));
      setItemEdits({});
      setSpellEdits({});
      setEditing(false);
    } catch {
      // The page-level message contains the API or import details.
    }
  }

  async function addInventoryRow() {
    setAddingRow(true);
    try {
      await onAddInventoryRow();
    } finally {
      setAddingRow(false);
    }
  }

  async function removeInventoryRow() {
    if (!canRemoveInventoryRow) return;
    setRemovingRow(true);
    try {
      await onRemoveInventoryRow();
    } finally {
      setRemovingRow(false);
    }
  }

  function selectCurrency(displayCurrency: string) {
    setDraft((current) => ({
      ...current,
      money: {
        ...current.money,
        displayCurrency,
        displayAmount: displayAmountForCurrency(current.money.amountBase, displayCurrency),
      },
    }));
  }

  function updateMoneyDisplay(displayAmount: number) {
    setDraft((current) => ({
      ...current,
      money: {
        ...current.money,
        amountBase: baseAmountFromDisplay(displayAmount, current.money.displayCurrency),
        displayAmount,
      },
    }));
  }

  async function moveInventoryItem(toSlotIndex: number) {
    if (draggedSlotIndex === null || draggedSlotIndex === toSlotIndex) return;
    setItemBusy(true);
    try {
      await onMoveInventoryItem(draggedSlotIndex, toSlotIndex);
    } finally {
      setDraggedSlotIndex(null);
      setItemBusy(false);
    }
  }

  async function createItem() {
    if (!itemForm.title.trim()) return;
    const targetSlotIndex = itemFormTargetSlot;
    const beforeItemIds = new Set(shown.inventory.slots.flatMap((slot) => slot.item ? [slot.item.id] : []));
    setItemBusy(true);
    try {
      const updatedSheet = await onCreateItem({
        ...itemForm,
        title: itemForm.title.trim(),
        imageUrl: itemForm.imageUrl.trim(),
        equipmentType: itemForm.type === "EQUIPMENT" ? itemForm.equipmentType ?? "HEAD" : null,
        sellPriceBase: itemForm.type === "TRADE" ? itemForm.sellPriceBase ?? 0 : null,
      });
      if (updatedSheet) {
        const createdSlot = updatedSheet.inventory.slots.find((slot) => slot.item && !beforeItemIds.has(slot.item.id));
        if (targetSlotIndex !== null && createdSlot?.item && createdSlot.slotIndex !== targetSlotIndex) {
          await onMoveInventoryItem(createdSlot.slotIndex, targetSlotIndex);
          setDraft(structuredClone(moveItemInSheet(updatedSheet, createdSlot.slotIndex, targetSlotIndex)));
        } else {
          setDraft(structuredClone(updatedSheet));
        }
        if (createdSlot?.item) setSelectedItemId(createdSlot.item.id);
      }
      setItemForm(EMPTY_ITEM_FORM);
      setItemFormOpen(false);
      setItemFormTargetSlot(null);
    } finally {
      setItemBusy(false);
    }
  }

  async function createSpell() {
    if (!spellForm.name.trim() || !spellForm.requirements.trim()) return;
    setItemBusy(true);
    try {
      const updatedSheet = await onCreateSpell({
        ...spellForm,
        name: spellForm.name.trim(),
        imageUrl: spellForm.imageUrl.trim(),
        requirements: spellForm.requirements.trim(),
      });
      if (updatedSheet) setDraft(structuredClone(updatedSheet));
      setSpellForm(EMPTY_SPELL_FORM);
      setSpellFormOpen(false);
    } finally {
      setItemBusy(false);
    }
  }

  async function deleteSpell(spellId: string) {
    if (!window.confirm("Delete this spell?")) return;
    setItemBusy(true);
    try {
      const updatedSheet = await onDeleteSpell(spellId);
      if (updatedSheet) setDraft(structuredClone(updatedSheet));
      setSpellEdits((current) => {
        const next = { ...current };
        delete next[spellId];
        return next;
      });
      setSelectedSpellId(null);
    } finally {
      setItemBusy(false);
    }
  }

  async function throwAwayItem(itemId: string) {
    if (!window.confirm("Throw away this item?")) return;
    setItemBusy(true);
    try {
      const updatedSheet = await onThrowAwayItem(itemId);
      if (updatedSheet) setDraft(structuredClone(updatedSheet));
      clearItemEdit(itemId);
      setSelectedItemId(null);
    } finally {
      setItemBusy(false);
    }
  }

  async function sellItem(itemId: string) {
    setItemBusy(true);
    try {
      const pendingEdit = itemEdits[itemId];
      if (pendingEdit) {
        await onUpdateItem(itemId, normalizeItemUpdate(pendingEdit));
      }
      const updatedSheet = await onSellItem(itemId);
      if (updatedSheet) setDraft(structuredClone(updatedSheet));
      clearItemEdit(itemId);
      setSelectedItemId(null);
    } finally {
      setItemBusy(false);
    }
  }

  async function equipItem(item: Item) {
    if (!selectedEquipSlot) return;
    setItemBusy(true);
    try {
      const updatedSheet = await onEquipItem(item.id, selectedEquipSlot);
      if (updatedSheet) setDraft(structuredClone(updatedSheet));
      setSelectedEquipSlot("");
    } finally {
      setItemBusy(false);
    }
  }

  async function unequipItem(slotCode: EquipmentSlotCode) {
    setItemBusy(true);
    try {
      const updatedSheet = await onUnequipItem(slotCode);
      if (updatedSheet) setDraft(structuredClone(updatedSheet));
    } finally {
      setItemBusy(false);
    }
  }

  function cancel() {
    setDraft(structuredClone(sheet));
    setEditing(false);
    setItemFormOpen(false);
    setSpellFormOpen(false);
    setItemEdits({});
    setSpellEdits({});
    setSelectedItemId(null);
    setSelectedSpellId(null);
  }

  function updateInfo(field: keyof CharacterInfo, value: string | number) {
    setDraft((current) => ({
      ...current,
      info: { ...current.info, [field]: value },
    }));
  }

  function updateStat(key: StatKey, level: number) {
    setDraft((current) => ({
      ...current,
      stats: { ...current.stats, [key]: { ...current.stats[key], level } },
    }));
  }

  function updateSkill(skillName: string, level: number) {
    setDraft((current) => ({
      ...current,
      skills: current.skills.map((skill) => skill.skillName === skillName
        ? { ...skill, level }
        : skill),
    }));
  }

  return (
    <>
      {mode === "guest" && (
        <div className="guest-notice">
          <strong>Guest mode</strong>
          <span>This sheet stays in this browser tab. Export JSON before closing it.</span>
        </div>
      )}
      <section className="sheet-title">
        <div>
          <p className="eyebrow">{mode === "guest" ? "Local guest sheet" : "Character sheet"}</p>
          {editing ? (
            <input
              className="sheet-name-input"
              value={draft.name}
              onChange={(event) => setDraft({ ...draft, name: event.target.value })}
            />
          ) : <h1>{sheet.name}</h1>}
          <p className="sheet-subtitle">
            {[shown.info.origin, shown.info.background, shown.info.className, shown.info.specialization]
              .filter(Boolean)
              .join(" / ") || "The story is still unwritten."}
          </p>
        </div>
        <div className="sheet-actions">
          {editing ? (
            <>
              <button className="button primary" onClick={() => void save()} disabled={!canSaveChanges}>
                {saving ? "Saving..." : "Save changes"}
              </button>
              <button className="button ghost" onClick={cancel} disabled={saving}>Cancel</button>
            </>
          ) : (
            canEdit && <button className="button primary" onClick={() => setEditing(true)}>Edit sheet</button>
          )}
          {canEdit && <button className="button ghost" onClick={() => void onExport()}>Export JSON</button>}
        </div>
      </section>

      <PortraitPanel
        sheet={shown}
        editing={editing}
        onChange={(imageUrl) => setDraft({ ...draft, imageUrl })}
      />

      <IdentityPanel sheet={shown} editing={editing} updateInfo={updateInfo} />

      <section className="dashboard-grid">
        <Metric className="level-metric" label="Level" value={shown.info.level}>
          {editing && (
            <NumberInput value={draft.info.level} min={1} onChange={(value) => updateInfo("level", value)} />
          )}
        </Metric>
        <div className="metric-pair">
          <Metric className="metric-illustrated metric-defense" imageUrl={METRIC_BACKGROUNDS.defence} label="Defense" value={shown.condition.passiveDefense}>
            {editing && <NumberInput value={draft.condition.passiveDefense} onChange={(value) => {
              setDraft({ ...draft, condition: { ...draft.condition, passiveDefense: value } });
            }} />}
          </Metric>
          <Metric className="metric-illustrated metric-dodge" imageUrl={METRIC_BACKGROUNDS.dodge} label="Dodge" value={previewDodge} derived />
        </div>
        <div className="metric-pair">
          <Metric className="metric-illustrated metric-blessings" imageUrl={METRIC_BACKGROUNDS.blessings} label="Blessings" value={shown.blessings.blessings}>
            {editing && <NumberInput value={draft.blessings.blessings} onChange={(value) => {
              setDraft({ ...draft, blessings: { ...draft.blessings, blessings: value } });
            }} />}
          </Metric>
          <Metric className="metric-illustrated metric-inspirations" imageUrl={METRIC_BACKGROUNDS.inspirations} label="Inspirations" value={shown.blessings.inspirations}>
            {editing && <NumberInput value={draft.blessings.inspirations} onChange={(value) => {
              setDraft({ ...draft, blessings: { ...draft.blessings, inspirations: value } });
            }} />}
          </Metric>
        </div>
        <Metric className="metric-illustrated metric-movement" imageUrl={METRIC_BACKGROUNDS.movement} label="Movement" value={shown.condition.movementSpeed}>
          {editing && <NumberInput value={draft.condition.movementSpeed} onChange={(value) => {
            setDraft({ ...draft, condition: { ...draft.condition, movementSpeed: value } });
          }} />}
        </Metric>
        <Metric className="metric-illustrated metric-health" imageUrl={METRIC_BACKGROUNDS.health} label="Health" value={`${previewHealth}%`} derived danger={previewHealth <= 30} />
        <Metric label="Money" value={shown.money.displayAmount} derived>
          <div className="money-value">
            {editing ? (
              <>
                <NumberInput value={shown.money.displayAmount} onChange={updateMoneyDisplay} />
                <select
                  value={shown.money.displayCurrency}
                  onChange={(event) => selectCurrency(event.target.value)}
                  aria-label="Display currency"
                >
                  {CURRENCY_CODES.map((currency) => (
                    <option key={currency} value={currency}>{label(currency)}</option>
                  ))}
                </select>
              </>
            ) : (
              <>
                <strong>{shown.money.displayAmount}</strong>
                <small>{label(shown.money.displayCurrency)}</small>
              </>
            )}
          </div>
        </Metric>
      </section>

      <section className="sheet-upper-grid">
        <section className="sheet-panel stats-panel">
          <PanelTitle eyebrow="Manual level and roll" title="Stats & skills" />
          <div className="stat-stack">
            {STAT_DEFINITIONS.map((definition) => (
              <StatBlock
                key={definition.key}
                statKey={definition.key}
                title={definition.title}
                level={shown.stats[definition.key].level}
                skills={shown.skills.filter((skill) => skill.statGroup === definition.group)}
                editing={editing}
                updateStat={updateStat}
                updateSkill={updateSkill}
              />
            ))}
          </div>
        </section>

        <div className="sheet-side-stack">
          <section className="sheet-panel">
            <PanelTitle eyebrow="Equipped items" title="Equipment" />
            {EQUIPMENT_GROUPS.map((group) => (
              <div className="equipment-group" key={group.title}>
                <p>{group.title}</p>
                <div
                  className="equipment-grid"
                  style={{ "--slot-count": group.slots.length } as React.CSSProperties}
                >
                  {group.slots.map((slot) => (
                    <EquipmentSlot
                      key={slot}
                      slot={slot}
                      title={shown.equipment[slot]?.title}
                      imageUrl={shown.equipment[slot]?.imageUrl}
                      onUnequip={editing && shown.equipment[slot] ? unequipItem : undefined}
                      busy={itemBusy}
                    />
                  ))}
                </div>
              </div>
            ))}
          </section>
          <ConditionPanel
            condition={shown.condition.hp}
            health={previewHealth}
            editing={editing}
            onChange={(hp) => setDraft({ ...draft, condition: { ...draft.condition, hp } })}
          />
        </div>
      </section>

      <section className="sheet-panel inventory-panel">
        <div className="panel-title inventory-title">
          <div>
            <h2>Inventory</h2>
          </div>
          <div className={overweight ? "weight-summary weight-editor danger" : "weight-summary weight-editor"}>
            <span>Weight: {currentCarryWeight} /</span>
            {editing ? (
              <NumberInput value={draft.condition.maxCarryWeight} onChange={(value) => {
                setDraft({ ...draft, condition: { ...draft.condition, maxCarryWeight: value } });
              }} />
            ) : <strong>{maxCarryWeight}</strong>}
          </div>
        </div>
        <div className="inventory-grid">
          {shown.inventory.slots.map((slot) => (
            <button
              className={slot.item || editing ? "inventory-slot interactive" + (slot.item ? " occupied" : " empty") : "inventory-slot"}
              key={slot.slotIndex}
              type="button"
              disabled={!slot.item && !editing}
              draggable={editing && !!slot.item}
              onDragStart={() => editing && slot.item && setDraggedSlotIndex(slot.slotIndex)}
              onDragOver={(event) => {
                if (editing && draggedSlotIndex !== null) event.preventDefault();
              }}
              onDrop={(event) => {
                event.preventDefault();
                if (editing) void moveInventoryItem(slot.slotIndex);
              }}
              onDragEnd={() => setDraggedSlotIndex(null)}
              onClick={() => {
                if (slot.item) {
                  setSelectedItemId(slot.item.id);
                  return;
                }
                if (editing) {
                  setItemFormTargetSlot(slot.slotIndex);
                  setItemForm(EMPTY_ITEM_FORM);
                  setItemFormOpen(true);
                }
              }}
            >
              {slot.item ? (
                <>
                  <ImageOrInitial
                    className="slot-image"
                    imageUrl={slot.item.imageUrl}
                    fallbackText={slot.item.title}
                  />
                  <small>{slot.item.title}</small>
                </>
              ) : <span>+</span>}
            </button>
          ))}
        </div>
        {selectedItem && (
          <ItemActionsPanel
            item={selectedItem}
            selectedSlot={selectedEquipSlot}
            onSlotChange={setSelectedEquipSlot}
            onUpdate={(item) => setItemEdits((current) => ({ ...current, [selectedItem.id]: item }))}
            onEquip={() => void equipItem(selectedItem)}
            onThrowAway={() => void throwAwayItem(selectedItem.id)}
            onSell={() => void sellItem(selectedItem.id)}
            onClose={() => setSelectedItemId(null)}
            busy={itemBusy}
            editing={editing}
          />
        )}
        {editing && (
          <div className="hero-actions">
            <button className="button ghost" onClick={() => void addInventoryRow()} disabled={addingRow}>
              {addingRow ? "Adding..." : "+ Add row"}
            </button>
            <button
              className="button ghost"
              onClick={() => void removeInventoryRow()}
              disabled={removingRow || !canRemoveInventoryRow}
              title={canRemoveInventoryRow ? "Remove the last empty row" : "The last row must be empty, and one row must remain"}
            >
              {removingRow ? "Removing..." : "- Remove row"}
            </button>
            <button className="button primary" onClick={() => {
              setItemFormTargetSlot(null);
              setItemFormOpen(true);
            }}>Add item</button>
          </div>
        )}
      </section>
      {editing && itemFormOpen && (
        <ItemFormDialog
          form={itemForm}
          busy={itemBusy}
          onChange={setItemForm}
          onCancel={() => {
            setItemFormOpen(false);
            setItemFormTargetSlot(null);
          }}
          onSubmit={() => void createItem()}
        />
      )}

      <section className="sheet-lower-grid">
        <section className="sheet-panel">
          <PanelTitle eyebrow="Prepared magic" title="Spells" />
          {shown.spells.length ? (
            <div className="spell-list">
              {shown.spells.map((spell) => (
                <button
                  className="spell-card"
                  key={spell.id}
                  type="button"
                  onClick={() => setSelectedSpellId(spell.id)}
                >
                  <ImageOrInitial
                    className="spell-icon"
                    imageUrl={spell.imageUrl}
                    fallbackText={spell.name}
                  />
                  <div>
                    <h3>{spell.name}</h3>
                    <p>{label(spell.type)} / {label(spell.spellClass)}</p>
                    <small>{spell.requirements}</small>
                  </div>
                </button>
              ))}
            </div>
          ) : <p className="muted-copy">No spells recorded yet.</p>}
          {selectedSpell && (
            <SpellActionsPanel
              spell={selectedSpell}
              editing={editing}
              busy={itemBusy}
              onChange={(spell) => setSpellEdits((current) => ({ ...current, [selectedSpell.id]: spell }))}
              onDelete={() => void deleteSpell(selectedSpell.id)}
              onClose={() => setSelectedSpellId(null)}
            />
          )}
          {editing && (
            <button className="button primary" onClick={() => setSpellFormOpen(true)}>Add spell</button>
          )}
        </section>
        <NarrativePanel
          sheet={shown}
          editing={editing}
          onChange={(field, value) => setDraft({
            ...draft,
            additionalInfo: { ...draft.additionalInfo, [field]: value },
          })}
        />
      </section>
      {editing && spellFormOpen && (
        <SpellFormDialog
          form={spellForm}
          busy={itemBusy}
          onChange={setSpellForm}
          onCancel={() => setSpellFormOpen(false)}
          onSubmit={() => void createSpell()}
        />
      )}
      {canEdit && <div className="sticky-edit-actions">
        <span>{editing ? "Editing character sheet" : "Viewing character sheet"}</span>
        {editing ? (
          <>
            <button className="button primary compact-button" onClick={() => void save()} disabled={!canSaveChanges}>
              {saving ? "Saving..." : "Save changes"}
            </button>
            <button className="button ghost compact-button" onClick={cancel} disabled={saving}>Cancel</button>
          </>
        ) : (
          <button className="button primary compact-button" onClick={() => setEditing(true)}>Edit sheet</button>
        )}
      </div>}
    </>
  );
}

function IdentityPanel({ sheet, editing, updateInfo }: {
  sheet: CharacterSheet;
  editing: boolean;
  updateInfo: (field: keyof CharacterInfo, value: string | number) => void;
}) {
  const fields = [
    ["origin", "Origin"], ["background", "Background"],
    ["className", "Class"], ["specialization", "Specialization"],
  ] as const;
  return (
    <section className="identity-panel">
      <div>
        <p className="eyebrow">Character info</p>
        <h2>Identity</h2>
      </div>
      <div className="identity-grid">
        {fields.map(([field, title]) => (
          <label key={field}>
            <span>{title}</span>
            {editing ? (
              <input value={sheet.info[field]} onChange={(event) => updateInfo(field, event.target.value)} />
            ) : <strong>{sheet.info[field]}</strong>}
          </label>
        ))}
      </div>
    </section>
  );
}

function PortraitPanel({ sheet, editing, onChange }: {
  sheet: CharacterSheet;
  editing: boolean;
  onChange: (imageUrl: string) => void;
}) {
  return (
    <section className="portrait-panel">
      {sheet.imageUrl ? (
        <img className="sheet-portrait" src={sheet.imageUrl} alt="" />
      ) : (
        <div className="sheet-portrait placeholder">{sheet.name.slice(0, 1).toUpperCase()}</div>
      )}
      <div>
        <p className="eyebrow">Character portrait</p>
        <h2>{sheet.imageUrl ? "Portrait linked" : "No portrait yet"}</h2>
        {editing ? (
          <label>
            <span>Image URL</span>
            <input
              value={sheet.imageUrl}
              onChange={(event) => onChange(event.target.value)}
              placeholder="https://..."
            />
          </label>
        ) : (
          <p>{sheet.imageUrl || "Add an image URL while editing the sheet."}</p>
        )}
      </div>
    </section>
  );
}

function Metric({ label: title, value, derived = false, danger = false, className = "", imageUrl, children }: {
  label: string;
  value: string | number;
  derived?: boolean;
  danger?: boolean;
  className?: string;
  imageUrl?: string;
  children?: React.ReactNode;
}) {
  return (
    <article
      className={`metric ${className} ${derived ? "derived" : ""} ${danger ? "danger" : ""}`}
      style={imageUrl ? { "--metric-bg": `url(${imageUrl})` } as React.CSSProperties : undefined}
    >
      <p>{title}</p>
      {children || <strong>{value}</strong>}
    </article>
  );
}

function PanelTitle({ eyebrow, title }: { eyebrow: string; title: string }) {
  return <div className="panel-title"><p className="eyebrow">{eyebrow}</p><h2>{title}</h2></div>;
}

function StatBlock({ statKey, title, level, skills, editing, updateStat, updateSkill }: {
  statKey: StatKey;
  title: string;
  level: number;
  skills: Skill[];
  editing: boolean;
  updateStat: (key: StatKey, level: number) => void;
  updateSkill: (skillName: string, level: number) => void;
}) {
  return (
    <article className="stat-block">
      <header>
        <h3>{title}</h3>
        {editing
          ? <NumberInput value={level} onChange={(value) => updateStat(statKey, value)} />
          : <span className="manual-value">{level}</span>}
        <span className="derived-value">{rollForLevel(level)}</span>
      </header>
      <div className="skill-list">
        {skills.map((skill) => (
          <div className="skill-row" key={skill.skillName}>
            <span>{label(skill.skillName)}</span>
            {editing
              ? <NumberInput value={skill.level} onChange={(value) => updateSkill(skill.skillName, value)} />
              : <b>{skill.level}</b>}
            <small>{rollForLevel(skill.level)}</small>
            <small>{effectiveSkillRoll(level, skill.level)}</small>
          </div>
        ))}
      </div>
    </article>
  );
}

function EquipmentSlot({ slot, title, imageUrl, onUnequip, busy }: {
  slot: EquipmentSlotCode;
  title?: string;
  imageUrl?: string;
  onUnequip?: (slot: EquipmentSlotCode) => Promise<void>;
  busy: boolean;
}) {
  return (
    <div className={title ? "equipment-slot occupied" : "equipment-slot"}>
      <ImageOrInitial
        className="slot-image"
        imageUrl={imageUrl}
        fallbackText={title || placeholder(slot)}
      />
      <small>{title || label(slot)}</small>
      {title && onUnequip && (
        <button className="mini-action" disabled={busy} onClick={() => void onUnequip(slot)}>Unequip</button>
      )}
    </div>
  );
}

function ConditionPanel({ condition, health, editing, onChange }: {
  condition: BodyHealth;
  health: number;
  editing: boolean;
  onChange: (condition: BodyHealth) => void;
}) {
  function updatePart(part: keyof BodyHealth, field: "current" | "max", value: number) {
    const currentPart = condition[part];
    const nextMax = field === "max" ? value : currentPart.max;
    const nextCurrent = field === "current" ? Math.min(value, nextMax) : Math.min(currentPart.current, nextMax);
    onChange({
      ...condition,
      [part]: { max: nextMax, current: nextCurrent },
    });
  }

  return (
    <section className="sheet-panel condition-panel">
      <PanelTitle eyebrow={`${health}% global health`} title="Condition" />
      <div className="health-list">
        {Object.entries(condition).map(([part, hp]) => (
          <div className={hp.current === 0 ? "health-row danger" : "health-row"} key={part}>
            <span>{label(part)}</span>
            {editing ? (
              <>
                <NumberInput value={hp.current} max={hp.max} onChange={(value) => updatePart(part as keyof BodyHealth, "current", value)} />
                <NumberInput value={hp.max} onChange={(value) => updatePart(part as keyof BodyHealth, "max", value)} />
              </>
            ) : (
              <>
                <strong>{hp.current}</strong>
                <small>/ {hp.max}</small>
              </>
            )}
          </div>
        ))}
      </div>
    </section>
  );
}

function NarrativePanel({ sheet, editing, onChange }: {
  sheet: CharacterSheet;
  editing: boolean;
  onChange: (field: keyof CharacterSheet["additionalInfo"], value: string) => void;
}) {
  return (
    <section className="sheet-panel narrative-panel">
      <PanelTitle eyebrow="Character history" title="Narrative" />
      <div className="narrative-list">
        {NARRATIVE_FIELDS.map(([field, title]) => (
          <article key={field}>
            <h3>{title}</h3>
            {editing ? (
              <textarea
                value={sheet.additionalInfo[field]}
                onChange={(event) => onChange(field, event.target.value)}
              />
            ) : <p>{sheet.additionalInfo[field] || "No notes yet."}</p>}
          </article>
        ))}
      </div>
    </section>
  );
}

function NumberInput({ value, min = 0, max, onChange }: {
  value: number;
  min?: number;
  max?: number;
  onChange: (value: number) => void;
}) {
  const [displayValue, setDisplayValue] = useState(String(value));

  useEffect(() => {
    setDisplayValue(String(value));
  }, [value]);

  function commit(rawValue: string) {
    if (rawValue === "") {
      onChange(min);
      return;
    }
    const parsed = Number(rawValue);
    if (!Number.isFinite(parsed)) return;
    const nextValue = clamp(parsed, min, max);
    setDisplayValue(String(nextValue));
    onChange(nextValue);
  }

  return (
    <input
      className="number-input"
      type="number"
      min={min}
      max={max}
      value={displayValue}
      onBlur={() => commit(displayValue)}
      onChange={(event) => {
        const nextValue = event.target.value;
        setDisplayValue(nextValue);
        if (nextValue !== "") commit(nextValue);
      }}
    />
  );
}

function ImageOrInitial({ imageUrl, fallbackText, className }: {
  imageUrl?: string | null;
  fallbackText: string;
  className: string;
}) {
  const [failed, setFailed] = useState(false);
  const preparedUrl = imageUrl?.trim();

  useEffect(() => {
    setFailed(false);
  }, [preparedUrl]);

  if (preparedUrl && preparedUrl !== "local-placeholder" && !failed) {
    return (
      <img
        className={className}
        src={preparedUrl}
        alt=""
        onError={() => setFailed(true)}
      />
    );
  }

  return <span className={`${className} placeholder`}>{fallbackText.slice(0, 1).toUpperCase()}</span>;
}

function SpellActionsPanel({ spell, editing, busy, onChange, onDelete, onClose }: {
  spell: Spell;
  editing: boolean;
  busy: boolean;
  onChange: (spell: SpellUpsert) => void;
  onDelete: () => void;
  onClose: () => void;
}) {
  const draft = spellToUpsert(spell);
  return (
    <article className="spell-actions-panel">
      <div className="detail-heading">
        <ImageOrInitial className="detail-image" imageUrl={spell.imageUrl} fallbackText={spell.name} />
        <p className="eyebrow">{label(spell.type)}</p>
      </div>
      {editing ? (
        <div className="spell-edit-form">
          <label>
            Name
            <input value={draft.name} onChange={(event) => onChange({ ...draft, name: event.target.value })} />
          </label>
          <label>
            Type
            <select value={draft.type} onChange={(event) => onChange({ ...draft, type: event.target.value })}>
              {SPELL_TYPE_OPTIONS.map((type) => <option key={type} value={type}>{label(type)}</option>)}
            </select>
          </label>
          <label>
            Class
            <select value={draft.spellClass} onChange={(event) => onChange({ ...draft, spellClass: event.target.value })}>
              {SPELL_CLASS_OPTIONS.map((spellClass) => <option key={spellClass} value={spellClass}>{label(spellClass)}</option>)}
            </select>
          </label>
          <label>
            Image URL
            <input value={draft.imageUrl} onChange={(event) => onChange({ ...draft, imageUrl: event.target.value })} />
          </label>
          <label className="form-span">
            Requirements
            <input value={draft.requirements} onChange={(event) => onChange({ ...draft, requirements: event.target.value })} />
          </label>
          <label className="form-span">
            Description
            <textarea value={draft.description} onChange={(event) => onChange({ ...draft, description: event.target.value })} />
          </label>
        </div>
      ) : (
        <>
          <h3>{spell.name}</h3>
          <p>{label(spell.spellClass)} / {spell.requirements}</p>
          <small>{spell.description || "No description."}</small>
        </>
      )}
      <div className="hero-actions">
        {editing && <button className="button ghost compact-button" disabled={busy} onClick={onDelete}>Delete spell</button>}
        <button className="text-action" disabled={busy} onClick={onClose}>Close</button>
      </div>
    </article>
  );
}

function ItemActionsPanel({ item, selectedSlot, onSlotChange, onUpdate, onEquip, onThrowAway, onSell, onClose, busy, editing }: {
  item: Item;
  selectedSlot: EquipmentSlotCode | "";
  onSlotChange: (slot: EquipmentSlotCode | "") => void;
  onUpdate: (item: ItemUpsert) => void;
  onEquip: () => void;
  onThrowAway: () => void;
  onSell: () => void;
  onClose: () => void;
  busy: boolean;
  editing: boolean;
}) {
  const equipSlots = slotsForEquipmentType(item.equipmentType);
  const itemDraft = itemToUpsert(item);

  return (
    <article className="item-actions-panel">
      <div className="item-details">
        <div className="detail-heading">
          <ImageOrInitial className="detail-image" imageUrl={item.imageUrl} fallbackText={item.title} />
          <p className="eyebrow">{label(item.type)}</p>
        </div>
        {editing ? (
          <div className="item-edit-form">
            <label>
              Title
              <input
                value={itemDraft.title}
                onChange={(event) => onUpdate({ ...itemDraft, title: event.target.value })}
              />
            </label>
            <label className="form-span">
              Image URL
              <input
                value={itemDraft.imageUrl}
                onChange={(event) => onUpdate({ ...itemDraft, imageUrl: event.target.value })}
              />
            </label>
            <label className="form-span">
              Description
              <textarea
                value={itemDraft.description}
                onChange={(event) => onUpdate({ ...itemDraft, description: event.target.value })}
              />
            </label>
            <label className="item-number-field">
              Weight
              <NumberInput value={itemDraft.weight} onChange={(weight) => onUpdate({ ...itemDraft, weight })} />
            </label>
            {item.type === "TRADE" && (
              <label className="item-number-field">
                Sell price
                <NumberInput
                  value={itemDraft.sellPriceBase ?? 0}
                  onChange={(sellPriceBase) => onUpdate({ ...itemDraft, sellPriceBase })}
                />
              </label>
            )}
          </div>
        ) : (
          <>
            <h3>{item.title}</h3>
            <p>{item.description || "No description."}</p>
            <small>Weight: {item.weight}</small>
            {item.sellPriceBase !== null && <small>Sell price: {item.sellPriceBase}</small>}
          </>
        )}
      </div>
      <div className="item-actions">
        {editing && item.type === "EQUIPMENT" && (
          <label>
            Equip slot
            <select value={selectedSlot} onChange={(event) => onSlotChange(event.target.value as EquipmentSlotCode)}>
              <option value="">Choose slot</option>
              {equipSlots.map((slot) => <option key={slot} value={slot}>{label(slot)}</option>)}
            </select>
            <button className="button primary compact-button" disabled={busy || !selectedSlot} onClick={onEquip}>Equip</button>
          </label>
        )}
        {editing && item.type === "TRADE" && (
          <button className="button primary compact-button" disabled={busy} onClick={onSell}>Sell</button>
        )}
        {editing && <button className="button ghost compact-button" disabled={busy} onClick={onThrowAway}>Throw away</button>}
        <button className="text-action" disabled={busy} onClick={onClose}>Close</button>
      </div>
    </article>
  );
}

function ItemFormDialog({ form, busy, onChange, onCancel, onSubmit }: {
  form: ItemUpsert;
  busy: boolean;
  onChange: (form: ItemUpsert) => void;
  onCancel: () => void;
  onSubmit: () => void;
}) {
  function changeType(type: ItemUpsert["type"]) {
    onChange({
      ...form,
      type,
      equipmentType: type === "EQUIPMENT" ? form.equipmentType ?? "HEAD" : null,
      sellPriceBase: type === "TRADE" ? form.sellPriceBase ?? 0 : null,
    });
  }

  return (
    <div className="dialog-backdrop" role="presentation">
      <section className="item-dialog" role="dialog" aria-modal="true" aria-label="Add inventory item">
        <div className="panel-title">
          <p className="eyebrow">Inventory</p>
          <h2>Add item</h2>
        </div>
        <div className="item-form-grid">
          <label>
            Type
            <select value={form.type} onChange={(event) => changeType(event.target.value as ItemUpsert["type"])}>
              <option value="ITEM">Item</option>
              <option value="EQUIPMENT">Equipment</option>
              <option value="TRADE">Trade</option>
            </select>
          </label>
          <label>
            Title
            <input value={form.title} onChange={(event) => onChange({ ...form, title: event.target.value })} autoFocus />
          </label>
          <label>
            Image URL
            <input value={form.imageUrl} onChange={(event) => onChange({ ...form, imageUrl: event.target.value })} />
          </label>
          <label>
            Weight
            <NumberInput value={form.weight} onChange={(weight) => onChange({ ...form, weight })} />
          </label>
          {form.type === "EQUIPMENT" && (
            <label>
              Equipment type
              <select value={form.equipmentType ?? "HEAD"} onChange={(event) => onChange({ ...form, equipmentType: event.target.value })}>
                {EQUIPMENT_TYPE_OPTIONS.map((type) => <option key={type} value={type}>{label(type)}</option>)}
              </select>
            </label>
          )}
          {form.type === "TRADE" && (
            <label>
              Sell price
              <NumberInput
                value={form.sellPriceBase ?? 0}
                onChange={(sellPriceBase) => onChange({ ...form, sellPriceBase })}
              />
            </label>
          )}
          <label className="form-span">
            Description
            <textarea value={form.description} onChange={(event) => onChange({ ...form, description: event.target.value })} />
          </label>
        </div>
        <div className="hero-actions">
          <button className="button primary" disabled={busy || !form.title.trim()} onClick={onSubmit}>
            {busy ? "Adding..." : "Add item"}
          </button>
          <button className="button ghost" disabled={busy} onClick={onCancel}>Cancel</button>
        </div>
      </section>
    </div>
  );
}

function SpellFormDialog({ form, busy, onChange, onCancel, onSubmit }: {
  form: SpellUpsert;
  busy: boolean;
  onChange: (form: SpellUpsert) => void;
  onCancel: () => void;
  onSubmit: () => void;
}) {
  return (
    <div className="dialog-backdrop" role="presentation">
      <section className="item-dialog" role="dialog" aria-modal="true" aria-label="Add spell">
        <div className="panel-title">
          <p className="eyebrow">Prepared magic</p>
          <h2>Add spell</h2>
        </div>
        <div className="item-form-grid">
          <label>
            Name
            <input value={form.name} onChange={(event) => onChange({ ...form, name: event.target.value })} autoFocus />
          </label>
          <label>
            Type
            <select value={form.type} onChange={(event) => onChange({ ...form, type: event.target.value })}>
              {SPELL_TYPE_OPTIONS.map((type) => <option key={type} value={type}>{label(type)}</option>)}
            </select>
          </label>
          <label>
            Class
            <select value={form.spellClass} onChange={(event) => onChange({ ...form, spellClass: event.target.value })}>
              {SPELL_CLASS_OPTIONS.map((spellClass) => <option key={spellClass} value={spellClass}>{label(spellClass)}</option>)}
            </select>
          </label>
          <label>
            Image URL
            <input value={form.imageUrl} onChange={(event) => onChange({ ...form, imageUrl: event.target.value })} />
          </label>
          <label className="form-span">
            Requirements
            <input value={form.requirements} onChange={(event) => onChange({ ...form, requirements: event.target.value })} />
          </label>
          <label className="form-span">
            Description
            <textarea value={form.description} onChange={(event) => onChange({ ...form, description: event.target.value })} />
          </label>
        </div>
        <div className="hero-actions">
          <button className="button primary" disabled={busy || !form.name.trim() || !form.requirements.trim()} onClick={onSubmit}>
            {busy ? "Adding..." : "Add spell"}
          </button>
          <button className="button ghost" disabled={busy} onClick={onCancel}>Cancel</button>
        </div>
      </section>
    </div>
  );
}

function placeholder(slot: EquipmentSlotCode): string {
  if (slot.startsWith("WEAPON")) return "W";
  if (slot.startsWith("TALISMAN")) return "T";
  return "A";
}

function itemToUpsert(item: Item): ItemUpsert {
  return {
    type: item.type,
    title: item.title,
    imageUrl: visibleImageUrl(item.imageUrl),
    weight: item.weight,
    description: item.description,
    equipmentType: item.equipmentType,
    sellPriceBase: item.sellPriceBase,
  };
}

function spellToUpsert(spell: Spell): SpellUpsert {
  return {
    name: spell.name,
    type: spell.type,
    spellClass: spell.spellClass,
    imageUrl: visibleImageUrl(spell.imageUrl),
    requirements: spell.requirements,
    description: spell.description,
  };
}

function visibleImageUrl(imageUrl: string): string {
  return imageUrl.trim() === "local-placeholder" ? "" : imageUrl;
}

function clamp(value: number, min: number, max?: number): number {
  return Math.min(Math.max(value, min), max ?? Number.POSITIVE_INFINITY);
}
