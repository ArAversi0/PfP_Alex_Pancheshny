import { useEffect, useMemo, useState, type ChangeEvent } from "react";
import { Link } from "react-router-dom";
import { getApiErrorMessage } from "../../auth/api/authApi";
import { FormMessage } from "../../auth/components/FormMessage";
import { SiteLayout } from "../../../shared/ui/SiteLayout";
import { adminApi } from "../api/adminApi";
import type { AdminContentNode, AdminContentSection } from "../model/adminTypes";

const emptyNode = (section: AdminContentSection): AdminContentNode => ({
  section,
  slug: "",
  parentSlug: null,
  title: "",
  summary: "",
  contentMarkdown: "",
  sortOrder: 0,
  published: true,
});

export function AdminContentPage() {
  const [section, setSection] = useState<AdminContentSection>("LORE");
  const [nodes, setNodes] = useState<AdminContentNode[]>([]);
  const [selectedSlug, setSelectedSlug] = useState("");
  const [draft, setDraft] = useState<AdminContentNode>(emptyNode("LORE"));
  const [creating, setCreating] = useState(false);
  const [busy, setBusy] = useState(false);
  const [message, setMessage] = useState("");
  const [error, setError] = useState("");

  useEffect(() => {
    load(section);
  }, [section]);

  const selected = useMemo(
    () => nodes.find((node) => node.slug === selectedSlug) ?? null,
    [nodes, selectedSlug],
  );

  async function load(targetSection: AdminContentSection) {
    setError("");
    setMessage("");
    try {
      const loaded = await adminApi.listContent(targetSection);
      setNodes(loaded);
      setSelectedSlug(loaded[0]?.slug ?? "");
      setDraft(loaded[0] ?? emptyNode(targetSection));
      setCreating(false);
    } catch (loadError) {
      setError(getApiErrorMessage(loadError));
    }
  }

  function selectNode(node: AdminContentNode) {
    setSelectedSlug(node.slug);
    setDraft(node);
    setCreating(false);
    setMessage("");
    setError("");
  }

  function startCreate() {
    setSelectedSlug("");
    setDraft({ ...emptyNode(section), sortOrder: nextSortOrder(nodes) });
    setCreating(true);
    setMessage("");
    setError("");
  }

  function updateDraft(event: ChangeEvent<HTMLInputElement | HTMLTextAreaElement | HTMLSelectElement>) {
    const { name, value } = event.target;
    setDraft((current) => ({
      ...current,
      [name]: name === "sortOrder" ? Number(value) : value,
    }));
  }

  function updatePublished(event: ChangeEvent<HTMLInputElement>) {
    setDraft((current) => ({ ...current, published: event.target.checked }));
  }

  async function save() {
    setBusy(true);
    setError("");
    setMessage("");
    try {
      const payload = { ...draft, parentSlug: draft.parentSlug || null, section };
      const saved = creating
        ? await adminApi.createContent(payload)
        : await adminApi.updateContent(payload);
      const loaded = await adminApi.listContent(section);
      setNodes(loaded);
      setSelectedSlug(saved.slug);
      setDraft(saved);
      setCreating(false);
      setMessage("Content saved.");
    } catch (saveError) {
      setError(getApiErrorMessage(saveError));
    } finally {
      setBusy(false);
    }
  }

  async function remove() {
    if (creating || !selected || !window.confirm(`Delete "${selected.title}"?`)) {
      return;
    }
    setBusy(true);
    setError("");
    setMessage("");
    try {
      await adminApi.deleteContent(selected);
      await load(section);
      setMessage("Content deleted.");
    } catch (deleteError) {
      setError(getApiErrorMessage(deleteError));
    } finally {
      setBusy(false);
    }
  }

  return (
    <SiteLayout wide>
      <div className="page-heading with-actions">
        <div>
          <p className="eyebrow">Administration</p>
          <h1>Content</h1>
          <p className="intro">Edit Lore and Rule book articles.</p>
        </div>
        <div className="archive-actions">
          <Link className="button ghost" to="/admin">Admin dashboard</Link>
          <button className="button primary" onClick={startCreate}>New article</button>
        </div>
      </div>
      {error && <FormMessage kind="error">{error}</FormMessage>}
      {message && <FormMessage kind="success">{message}</FormMessage>}
      <section className="admin-workbench">
        <aside className="admin-sidebar">
          <label>
            Section
            <select value={section} onChange={(event) => setSection(event.target.value as AdminContentSection)}>
              <option value="LORE">Lore</option>
              <option value="RULES">Rule book</option>
            </select>
          </label>
          <div className="admin-node-list">
            {nodes.map((node) => (
              <button
                key={node.slug}
                className={node.slug === selectedSlug ? "active" : undefined}
                onClick={() => selectNode(node)}
              >
                <strong>{node.title}</strong>
                <span>{node.parentSlug ? `Parent: ${node.parentSlug}` : "Root article"}</span>
              </button>
            ))}
          </div>
        </aside>
        <article className="admin-editor">
          <div className="admin-form-grid">
            <label>
              Slug
              <input name="slug" value={draft.slug} onChange={updateDraft} disabled={!creating} />
            </label>
            <label>
              Parent
              <select name="parentSlug" value={draft.parentSlug ?? ""} onChange={updateDraft}>
                <option value="">Root</option>
                {nodes.filter((node) => node.slug !== draft.slug).map((node) => (
                  <option key={node.slug} value={node.slug}>{node.title}</option>
                ))}
              </select>
            </label>
            <label>
              Title
              <input name="title" value={draft.title} onChange={updateDraft} />
            </label>
            <label>
              Sort order
              <input name="sortOrder" type="number" value={draft.sortOrder} onChange={updateDraft} />
            </label>
            <label className="form-span">
              Summary
              <textarea name="summary" value={draft.summary} onChange={updateDraft} />
            </label>
            <label className="form-span">
              Markdown
              <textarea className="admin-markdown-input" name="contentMarkdown" value={draft.contentMarkdown} onChange={updateDraft} />
            </label>
            <label className="admin-checkbox">
              <input type="checkbox" checked={draft.published} onChange={updatePublished} />
              Published
            </label>
          </div>
          <div className="hero-actions">
            <button className="button primary" disabled={busy || !draft.slug || !draft.title} onClick={() => void save()}>
              {busy ? "Saving..." : "Save"}
            </button>
            <button className="button ghost danger-button" disabled={busy || creating} onClick={() => void remove()}>
              Delete
            </button>
          </div>
        </article>
      </section>
    </SiteLayout>
  );
}

function nextSortOrder(nodes: AdminContentNode[]) {
  return nodes.reduce((max, node) => Math.max(max, node.sortOrder), 0) + 10;
}
