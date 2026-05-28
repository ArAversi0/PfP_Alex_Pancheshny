import { useEffect, useMemo, useState, type FormEvent } from "react";
import { Link } from "react-router-dom";
import { getApiErrorMessage } from "../../auth/api/authApi";
import { FormMessage } from "../../auth/components/FormMessage";
import { SiteLayout } from "../../../shared/ui/SiteLayout";
import { adminApi } from "../api/adminApi";
import type { AdminCharacterGroup } from "../model/adminTypes";

const USERS_PER_PAGE = 5;

export function AdminCharactersPage() {
  const [groups, setGroups] = useState<AdminCharacterGroup[]>([]);
  const [queryInput, setQueryInput] = useState("");
  const [query, setQuery] = useState("");
  const [page, setPage] = useState(1);
  const [error, setError] = useState("");

  useEffect(() => {
    adminApi.listCharacters().then(
      setGroups,
      (loadError: unknown) => setError(getApiErrorMessage(loadError)),
    );
  }, []);

  const filteredGroups = useMemo(() => {
    const normalized = query.trim().toLowerCase();
    if (!normalized) {
      return groups;
    }
    return groups.filter((group) => group.email.toLowerCase().includes(normalized));
  }, [groups, query]);
  const totalPages = Math.max(1, Math.ceil(filteredGroups.length / USERS_PER_PAGE));
  const visibleGroups = filteredGroups.slice((page - 1) * USERS_PER_PAGE, page * USERS_PER_PAGE);

  function find(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setQuery(queryInput);
    setPage(1);
  }

  return (
    <SiteLayout wide>
      <div className="page-heading with-actions">
        <div>
          <p className="eyebrow">Administration</p>
          <h1>Characters</h1>
          <p className="intro">Read-only view of all character sheets grouped by owner.</p>
        </div>
        <form className="admin-search-actions" onSubmit={find}>
          <Link className="button ghost" to="/admin">Admin dashboard</Link>
          <input
            className="admin-search"
            placeholder="Search email"
            value={queryInput}
            onChange={(event) => setQueryInput(event.target.value)}
          />
          <button className="button primary" type="submit">Find</button>
        </form>
      </div>
      {error && <FormMessage kind="error">{error}</FormMessage>}
      <section className="admin-character-groups">
        {visibleGroups.map((group) => (
          <article className="admin-character-group" key={group.userId}>
            <header className="admin-character-owner">
              <h2>{group.email || "Unknown owner"}</h2>
              <p>{group.characters.length} of 100 slots used</p>
            </header>
            <div className="character-grid">
              {group.characters.map((character) => (
                <Link className="character-card character-card-main" key={character.id} to={`/admin/characters/${character.id}`}>
                  <Portrait imageUrl={character.imageUrl} name={character.name} />
                  <div>
                    <p className="eyebrow">Level {character.level}</p>
                    <h2>{character.name}</h2>
                    <p>{character.className || "Unwritten class"}</p>
                    <small>{character.specialization || "No specialization yet"}</small>
                  </div>
                </Link>
              ))}
            </div>
            <div className="admin-character-separator" aria-hidden="true" />
          </article>
        ))}
        {filteredGroups.length === 0 && (
          <p className="muted-copy">No users found.</p>
        )}
      </section>
      {filteredGroups.length > USERS_PER_PAGE && (
        <nav className="admin-pagination" aria-label="Character owner pages">
          <button className="button ghost compact-button" disabled={page === 1} onClick={() => setPage((current) => Math.max(1, current - 1))}>
            Previous
          </button>
          <span>Page {page} of {totalPages}</span>
          <button className="button ghost compact-button" disabled={page === totalPages} onClick={() => setPage((current) => Math.min(totalPages, current + 1))}>
            Next
          </button>
        </nav>
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
