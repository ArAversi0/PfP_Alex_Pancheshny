export interface User {
  id: string;
  email: string;
  role: "ROLE_USER" | "ROLE_ADMIN";
  emailVerified: boolean;
}

export interface RefreshedTokens {
  accessToken: string;
  refreshToken: string;
  tokenType: "Bearer";
}

export interface AuthSession extends RefreshedTokens {
  user: User;
}

export interface GenericMessage {
  message: string;
}
