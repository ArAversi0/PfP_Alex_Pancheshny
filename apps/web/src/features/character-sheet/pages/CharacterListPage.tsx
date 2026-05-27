import { useEffect, useRef, useState, type ChangeEvent } from "react";
import { Link, useNavigate } from "react-router-dom";
import { getApiErrorMessage } from "../../auth/api/authApi";
import { FormMessage } from "../../auth/components/FormMessage";
import { characterApi } from "../api/characterApi";
import { CHARACTER_LIMIT, type CharacterCard } from "../model/characterTypes";
import { SiteLayout } from "../../../shared/ui/SiteLayout";

export function CharacterListPage() {
  const [characters, setCharacters] = useState<CharacterCard[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [busyId, setBusyId] = useState("");
  const [deleteTarget, setDeleteTarget] = useState<CharacterCard | null>(null);
  const fileInput = useRef<HTMLInputElement>(null);
  const navigate = useNavigate();
  const characterCount = characters.length;
  const limitReached = characterCount >= CHARACTER_LIMIT;

  useEffect(() => {
    characterApi.list().then(
      (loaded) => setCharacters(loaded),
      (loadError: unknown) => setError(getApiErrorMessage(loadError)),
    ).finally(() => setLoading(false));
  }, []);

  async function remove(character: CharacterCard) {
    setBusyId(character.id);
    setError("");
    try {
      await characterApi.remove(character.id);
      setCharacters((current) => current.filter(({ id }) => id !== character.id));
      setDeleteTarget(null);
    } catch (removeError) {
      setError(getApiErrorMessage(removeError));
    } finally {
      setBusyId("");
    }
  }

  async function importCharacter(event: ChangeEvent<HTMLInputElement>) {
    const file = event.target.files?.[0];
    event.target.value = "";
    if (!file) {
      return;
    }
    if (limitReached) {
      setError("Character limit reached. Delete a character before importing another.");
      return;
    }
    setError("");
    try {
      const created = await characterApi.importJson(await file.text());
      navigate(`/characters/${created.id}`);
    } catch (importError) {
      setError(getApiErrorMessage(importError));
    }
  }

  return (
    <SiteLayout wide>
      <div className="page-heading with-actions">
        <div>
          <p className="eyebrow">Authenticated archive</p>
          <h1>Your characters</h1>
          <p className="intro">Choose an adventurer or begin a new sheet.</p>
        </div>
        <div className="archive-actions">
          <div className={limitReached ? "character-count limit" : "character-count"}>
            <strong>{characterCount}</strong>
            <span>/ {CHARACTER_LIMIT}</span>
          </div>
          {limitReached
            ? <span className="button primary disabled">New character</span>
            : <Link className="button primary" to="/characters/new">New character</Link>}
          <button
            className="button ghost"
            disabled={loading || limitReached}
            title={limitReached ? "Character limit reached" : undefined}
            onClick={() => fileInput.current?.click()}
          >
            Import JSON
          </button>
          <input
            ref={fileInput}
            className="visually-hidden"
            type="file"
            accept="application/json,.json"
            onChange={importCharacter}
          />
        </div>
      </div>
      {error && <FormMessage kind="error">{error}</FormMessage>}
      {loading ? (
        <p className="loading-copy">Opening archive...</p>
      ) : characters.length === 0 ? (
        <section className="empty-state">
          <p className="eyebrow">Empty archive</p>
          <h2>Your first character is waiting.</h2>
          <p>Create a blank sheet or import a canonical PfP JSON document.</p>
          <Link className="button primary" to="/characters/new">Create character</Link>
        </section>
      ) : (
        <section className="character-grid">
          {characters.map((character) => (
            <article className="character-card" key={character.id}>
              <Link className="character-card-main" to={`/characters/${character.id}`}>
                <Portrait imageUrl={character.imageUrl} name={character.name} />
                <div>
                  <p className="eyebrow">Level {character.level}</p>
                  <h2>{character.name}</h2>
                  <p>{character.className || "Unwritten class"}</p>
                  <small>{character.specialization || "No specialization yet"}</small>
                </div>
              </Link>
              <button
                className="text-action danger"
                disabled={busyId === character.id}
                onClick={() => setDeleteTarget(character)}
              >
                {busyId === character.id ? "Deleting..." : "Delete"}
              </button>
            </article>
          ))}
        </section>
      )}
      {deleteTarget && (
        <div className="dialog-backdrop" role="presentation">
          <section className="confirm-dialog" role="dialog" aria-modal="true" aria-label="Delete character">
            <p className="eyebrow">Delete character</p>
            <h2>{deleteTarget.name}</h2>
            <p>This will permanently remove the character sheet and all saved inventory, equipment, spells, and notes.</p>
            <div className="hero-actions">
              <button
                className="button primary danger-button"
                disabled={busyId === deleteTarget.id}
                onClick={() => void remove(deleteTarget)}
              >
                {busyId === deleteTarget.id ? "Deleting..." : "Delete"}
              </button>
              <button
                className="button ghost"
                disabled={busyId === deleteTarget.id}
                onClick={() => setDeleteTarget(null)}
              >
                Cancel
              </button>
            </div>
          </section>
        </div>
      )}
    </SiteLayout>
  );
}

function Portrait({ imageUrl, name }: { imageUrl: string; name: string }) {
  if (imageUrl) {
    return <img className="character-portrait" src={imageUrl} alt="" />;
  }
  return <div className="character-portrait placeholder">{name.slice(0, 1).toUpperCase()}</div>;
}
