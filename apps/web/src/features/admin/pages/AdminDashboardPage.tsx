import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import { getApiErrorMessage } from "../../auth/api/authApi";
import { FormMessage } from "../../auth/components/FormMessage";
import { SiteLayout } from "../../../shared/ui/SiteLayout";
import { adminApi } from "../api/adminApi";
import type { AdminDashboardSummary } from "../model/adminTypes";

export function AdminDashboardPage() {
  const [summary, setSummary] = useState<AdminDashboardSummary | null>(null);
  const [error, setError] = useState("");

  useEffect(() => {
    adminApi.dashboard().then(
      setSummary,
      (loadError: unknown) => setError(getApiErrorMessage(loadError)),
    );
  }, []);

  return (
    <SiteLayout>
      <div className="page-heading">
        <p className="eyebrow">Administration</p>
        <h1>Admin</h1>
        <p className="intro">Manage published content and user accounts.</p>
      </div>
      {error && <FormMessage kind="error">{error}</FormMessage>}
      <section className="admin-dashboard">
        <article>
          <p className="eyebrow">Users</p>
          <strong>{summary?.users ?? "..."}</strong>
          <Link className="button ghost compact-button" to="/admin/users">Manage users</Link>
        </article>
        <article>
          <p className="eyebrow">Characters</p>
          <strong>{summary?.characters ?? "..."}</strong>
          <Link className="button ghost compact-button" to="/admin/characters">View all characters</Link>
        </article>
        <article>
          <p className="eyebrow">Content</p>
          <strong>{summary?.publishedContent ?? "..."}</strong>
          <Link className="button ghost compact-button" to="/admin/content">Edit content</Link>
        </article>
      </section>
    </SiteLayout>
  );
}
