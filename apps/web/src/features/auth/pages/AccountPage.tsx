import { useState } from "react";
import { getApiErrorMessage } from "../api/authApi";
import { FormMessage } from "../components/FormMessage";
import { useAuth } from "../model/useAuth";
import { SiteLayout } from "../../../shared/ui/SiteLayout";

export function AccountPage() {
  const { session, logout, refreshCurrentUser } = useAuth();
  const [error, setError] = useState("");
  const [refreshing, setRefreshing] = useState(false);

  if (!session) {
    return null;
  }

  async function refresh() {
    setRefreshing(true);
    setError("");
    try {
      await refreshCurrentUser();
    } catch (refreshError) {
      setError(getApiErrorMessage(refreshError));
    } finally {
      setRefreshing(false);
    }
  }

  return (
    <SiteLayout>
      <section className="account-shell">
        <p className="eyebrow">Authenticated account</p>
        <h1>Your archive</h1>
        <section className="account-card">
          <div className="account-initial">{session.user.email.slice(0, 1).toUpperCase()}</div>
          <div>
            <p className="account-email">{session.user.email}</p>
            <p className="account-meta">
              {session.user.role === "ROLE_ADMIN" ? "Administrator" : "Adventurer"}
              {" / "}
              {session.user.emailVerified ? "Verified email" : "Verification pending"}
            </p>
          </div>
        </section>
        {error && <FormMessage kind="error">{error}</FormMessage>}
        <div className="hero-actions">
          <button className="button ghost" onClick={refresh} disabled={refreshing}>
            {refreshing ? "Refreshing..." : "Refresh profile"}
          </button>
          <button className="button primary" onClick={() => void logout()}>
            Sign out
          </button>
        </div>
      </section>
    </SiteLayout>
  );
}
