import { useEffect, useState } from "react";
import { Link, useParams } from "react-router-dom";
import { getApiErrorMessage } from "../../auth/api/authApi";
import { FormMessage } from "../../auth/components/FormMessage";
import { CharacterSheetView } from "../../character-sheet/components/CharacterSheetView";
import type { CharacterSheet } from "../../character-sheet/model/characterTypes";
import { SiteLayout } from "../../../shared/ui/SiteLayout";
import { adminApi } from "../api/adminApi";

export function AdminCharacterSheetPage() {
  const { characterId = "" } = useParams();
  const [sheet, setSheet] = useState<CharacterSheet | null>(null);
  const [error, setError] = useState("");

  useEffect(() => {
    adminApi.getCharacterSheet(characterId).then(
      setSheet,
      (loadError: unknown) => setError(getApiErrorMessage(loadError)),
    );
  }, [characterId]);

  return (
    <SiteLayout wide>
      <div className="sheet-back-row">
        <Link className="button ghost compact-button" to="/admin/characters">All characters</Link>
        <Link className="button ghost compact-button" to="/admin">Admin dashboard</Link>
      </div>
      {error && <FormMessage kind="error">{error}</FormMessage>}
      {sheet ? (
        <CharacterSheetView
          sheet={sheet}
          mode="account"
          readOnly
          onSave={noop}
          onAddInventoryRow={noop}
          onRemoveInventoryRow={noop}
          onExport={noop}
          onCreateItem={noop}
          onUpdateItem={noop}
          onThrowAwayItem={noop}
          onSellItem={noop}
          onEquipItem={noop}
          onUnequipItem={noop}
          onMoveInventoryItem={noop}
          onCreateSpell={noop}
          onUpdateSpell={noop}
          onDeleteSpell={noop}
        />
      ) : !error && (
        <p className="loading-copy">Opening character sheet...</p>
      )}
    </SiteLayout>
  );
}

async function noop() {
}
