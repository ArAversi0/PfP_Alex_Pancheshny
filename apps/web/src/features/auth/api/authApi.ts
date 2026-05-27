import axios from "axios";
import { httpClient } from "../../../shared/api/httpClient";
import type {
  AuthSession,
  GenericMessage,
  User,
} from "../model/authTypes";

const AUTH_PATH = "/v1/auth";

export const authApi = {
  async register(email: string, password: string, confirmPassword: string) {
    return (
      await httpClient.post<User>(`${AUTH_PATH}/register`, {
        email,
        password,
        confirmPassword,
      })
    ).data;
  },

  async login(email: string, password: string) {
    return (
      await httpClient.post<AuthSession>(`${AUTH_PATH}/login`, {
        email,
        password,
      })
    ).data;
  },

  async exchangeOAuth2Code(code: string) {
    return (
      await httpClient.post<AuthSession>(`${AUTH_PATH}/oauth2/exchange`, {
        code,
      })
    ).data;
  },

  async verifyEmail(token: string) {
    return (
      await httpClient.get<GenericMessage>(`${AUTH_PATH}/verify-email`, {
        params: { token },
      })
    ).data;
  },

  async resendVerification(email: string) {
    return (
      await httpClient.post<GenericMessage>(`${AUTH_PATH}/verify-email/resend`, {
        email,
      })
    ).data;
  },

  async forgotPassword(email: string) {
    return (
      await httpClient.post<GenericMessage>(`${AUTH_PATH}/password/forgot`, {
        email,
      })
    ).data;
  },

  async resetPassword(
    token: string,
    newPassword: string,
    confirmPassword: string,
  ) {
    return (
      await httpClient.post<GenericMessage>(`${AUTH_PATH}/password/reset`, {
        token,
        newPassword,
        confirmPassword,
      })
    ).data;
  },

  async logout(refreshToken: string) {
    await httpClient.post(`${AUTH_PATH}/logout`, { refreshToken });
  },

  async currentUser() {
    return (await httpClient.get<User>(`${AUTH_PATH}/me`)).data;
  },
};

export function getOAuth2AuthorizationUrl(): string {
  const baseUrl = import.meta.env.VITE_API_URL ?? "/api";
  return `${baseUrl.replace(/\/$/, "")}${AUTH_PATH}/oauth2/authorize/google`;
}

export function getApiErrorMessage(error: unknown): string {
  if (axios.isAxiosError(error)) {
    const status = error.response?.status;
    const data = error.response?.data as unknown;
    if (data && typeof data === "object") {
      const body = data as {
        error?: string;
        fieldErrors?: Array<{ field?: string; message?: string }>;
        message?: string;
      };
      const fieldError = body.fieldErrors?.find((field) => field.message);
      if (fieldError?.message) {
        const message = humanizeApiMessage(fieldError.message);
        return fieldError.field ? `${fieldError.field}: ${message}` : message;
      }
      if (body.message?.trim()) {
        return humanizeApiMessage(body.message);
      }
      if (body.error?.trim() && status) {
        return `${status} ${body.error}`;
      }
    }
    if (typeof data === "string" && data.trim()) {
      return humanizeApiMessage(data.trim());
    }
    if (status === 401) {
      return "Authentication is required. Please sign in again.";
    }
    if (status === 403) {
      return "You do not have permission to perform this action.";
    }
    return status ? `The server could not process the request (${status}).` : "The server could not process the request.";
  }
  return error instanceof Error ? error.message : "Something went wrong.";
}

function humanizeApiMessage(message: string): string {
  const normalized = stripDiagnosticDetails(message.trim());
  const lower = normalized.toLowerCase();
  const knownMessages: Record<string, string> = {
    "account exists but verification email could not be delivered":
      "We created the account, but could not send the verification email. Please try again in a moment.",
    "email is already registered":
      "An account with this email already exists. Sign in instead, or use password reset if you need access.",
    "email verification is required":
      "Please verify your email before signing in.",
    "google oauth2 is not configured. set pfp_google_client_id and pfp_google_client_secret, then restart the backend.":
      "Google sign-in is not configured for this local backend.",
    "invalid email or password":
      "The email or password is incorrect.",
    "oauth2 exchange code is invalid or expired":
      "Google sign-in took too long or the login link expired. Please try again.",
    "oauth2 provider email must be verified":
      "Google did not confirm this email address. Try another Google account.",
    "oauth2 provider is not supported":
      "This sign-in provider is not supported.",
    "oauth2 subject is unavailable":
      "Google did not return enough account information. Please try again.",
    "password confirmation does not match":
      "The password confirmation does not match.",
    "password reset email could not be delivered":
      "We could not send the password reset email. Please try again in a moment.",
    "password reset token is invalid or expired":
      "This password reset link is invalid or has expired. Request a new one.",
    "password must contain at least 8 characters":
      "Password must be at least 8 characters long.",
    "refresh token is invalid or expired":
      "Your session has expired. Please sign in again.",
    "user account is unavailable":
      "This account is no longer available.",
    "verification token is invalid or expired":
      "This verification link is invalid or has expired. Request a new one.",
  };
  return knownMessages[lower] ?? normalized;
}

function stripDiagnosticDetails(message: string): string {
  return message
    .replace(/^[A-Za-z0-9_.]*Exception:\s*/, "")
    .replace(/\s+at\s+com\.pfp\..*$/, "")
    .trim();
}
