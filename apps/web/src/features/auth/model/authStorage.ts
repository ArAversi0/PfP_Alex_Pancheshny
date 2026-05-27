import type { AuthSession } from "./authTypes";

const STORAGE_KEY = "pfp.auth.session";
const SESSION_CHANGED_EVENT = "pfp-auth-session-changed";

export function getStoredSession(): AuthSession | null {
  const value = localStorage.getItem(STORAGE_KEY);
  if (!value) {
    return null;
  }
  try {
    const session = JSON.parse(value) as Partial<AuthSession>;
    if (
      !session.accessToken ||
      !session.refreshToken ||
      session.tokenType !== "Bearer" ||
      !session.user?.id
    ) {
      clearStoredSession();
      return null;
    }
    return session as AuthSession;
  } catch {
    clearStoredSession();
    return null;
  }
}

export function saveStoredSession(session: AuthSession): void {
  localStorage.setItem(STORAGE_KEY, JSON.stringify(session));
  window.dispatchEvent(new Event(SESSION_CHANGED_EVENT));
}

export function clearStoredSession(): void {
  localStorage.removeItem(STORAGE_KEY);
  window.dispatchEvent(new Event(SESSION_CHANGED_EVENT));
}

export function subscribeToSessionChanges(listener: () => void): () => void {
  window.addEventListener(SESSION_CHANGED_EVENT, listener);
  window.addEventListener("storage", listener);
  return () => {
    window.removeEventListener(SESSION_CHANGED_EVENT, listener);
    window.removeEventListener("storage", listener);
  };
}
