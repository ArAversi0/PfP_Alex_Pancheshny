import {
  useCallback,
  useEffect,
  useMemo,
  useState,
  type PropsWithChildren,
} from "react";
import { authApi } from "../api/authApi";
import { AuthContext } from "./AuthContext";
import {
  clearStoredSession,
  getStoredSession,
  saveStoredSession,
  subscribeToSessionChanges,
} from "./authStorage";

export function AuthProvider({ children }: PropsWithChildren) {
  const [session, setSession] = useState(getStoredSession);

  useEffect(() => subscribeToSessionChanges(() => setSession(getStoredSession())), []);

  const login = useCallback(async (email: string, password: string) => {
    saveStoredSession(await authApi.login(email, password));
  }, []);

  const completeOAuth2Login = useCallback(async (code: string) => {
    saveStoredSession(await authApi.exchangeOAuth2Code(code));
  }, []);

  const logout = useCallback(async () => {
    const refreshToken = getStoredSession()?.refreshToken;
    try {
      if (refreshToken) {
        await authApi.logout(refreshToken);
      }
    } finally {
      clearStoredSession();
    }
  }, []);

  const refreshCurrentUser = useCallback(async () => {
    const current = getStoredSession();
    if (current) {
      saveStoredSession({ ...current, user: await authApi.currentUser() });
    }
  }, []);

  const value = useMemo(
    () => ({
      session,
      login,
      completeOAuth2Login,
      logout,
      refreshCurrentUser,
    }),
    [completeOAuth2Login, login, logout, refreshCurrentUser, session],
  );

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}
