import { createContext } from "react";
import type { AuthSession } from "./authTypes";

export interface AuthContextValue {
  session: AuthSession | null;
  login(email: string, password: string): Promise<void>;
  completeOAuth2Login(code: string): Promise<void>;
  logout(): Promise<void>;
  refreshCurrentUser(): Promise<void>;
}

export const AuthContext = createContext<AuthContextValue | null>(null);
