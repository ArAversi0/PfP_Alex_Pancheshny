import { useEffect, useMemo, useState } from "react";
import { Link } from "react-router-dom";
import { getApiErrorMessage } from "../../auth/api/authApi";
import { FormMessage } from "../../auth/components/FormMessage";
import { useAuth } from "../../auth/model/useAuth";
import { SiteLayout } from "../../../shared/ui/SiteLayout";
import { adminApi } from "../api/adminApi";
import type { AdminUser } from "../model/adminTypes";

export function AdminUsersPage() {
  const { session } = useAuth();
  const [users, setUsers] = useState<AdminUser[]>([]);
  const [query, setQuery] = useState("");
  const [busyId, setBusyId] = useState("");
  const [error, setError] = useState("");
  const [message, setMessage] = useState("");

  useEffect(() => {
    load();
  }, []);

  const shownUsers = useMemo(() => {
    const normalized = query.trim().toLowerCase();
    if (!normalized) {
      return users;
    }
    return users.filter((user) => user.email.toLowerCase().includes(normalized));
  }, [query, users]);

  async function load() {
    setError("");
    try {
      setUsers(await adminApi.listUsers());
    } catch (loadError) {
      setError(getApiErrorMessage(loadError));
    }
  }

  async function remove(user: AdminUser) {
    if (!window.confirm(`Delete ${user.email} and all their characters? This cannot be undone.`)) {
      return;
    }
    setBusyId(user.id);
    setError("");
    setMessage("");
    try {
      await adminApi.deleteUser(user.id);
      setUsers((current) => current.filter(({ id }) => id !== user.id));
      setMessage("User deleted.");
    } catch (deleteError) {
      setError(getApiErrorMessage(deleteError));
    } finally {
      setBusyId("");
    }
  }

  return (
    <SiteLayout wide>
      <div className="page-heading with-actions">
        <div>
          <p className="eyebrow">Administration</p>
          <h1>Users</h1>
          <p className="intro">Review accounts and remove profiles when needed.</p>
        </div>
        <div className="archive-actions">
          <Link className="button ghost" to="/admin">Admin dashboard</Link>
          <input
            className="admin-search"
            placeholder="Search email"
            value={query}
            onChange={(event) => setQuery(event.target.value)}
          />
        </div>
      </div>
      {error && <FormMessage kind="error">{error}</FormMessage>}
      {message && <FormMessage kind="success">{message}</FormMessage>}
      <section className="admin-table">
        <div className="admin-table-row heading">
          <span>Email</span>
          <span>Role</span>
          <span>Verified</span>
          <span>Characters</span>
          <span>Created</span>
          <span>Actions</span>
        </div>
        {shownUsers.map((user) => {
          const self = user.id === session?.user.id;
          return (
            <div className="admin-table-row" key={user.id}>
              <span>{user.email}</span>
              <span>{user.role === "ROLE_ADMIN" ? "Admin" : "User"}</span>
              <span>{user.emailVerified ? "Yes" : "No"}</span>
              <span>{user.characterCount}</span>
              <span>{new Date(user.createdAt).toLocaleDateString()}</span>
              <span>
                <button
                  className="text-action danger"
                  disabled={self || busyId === user.id}
                  title={self ? "You cannot delete your own account" : undefined}
                  onClick={() => void remove(user)}
                >
                  {busyId === user.id ? "Deleting..." : "Delete"}
                </button>
              </span>
            </div>
          );
        })}
      </section>
    </SiteLayout>
  );
}
