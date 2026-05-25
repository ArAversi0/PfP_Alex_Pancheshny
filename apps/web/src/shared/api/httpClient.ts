import axios, {
  AxiosError,
  type InternalAxiosRequestConfig,
} from "axios";
import {
  clearStoredSession,
  getStoredSession,
  saveStoredSession,
} from "../../features/auth/model/authStorage";
import type { RefreshedTokens } from "../../features/auth/model/authTypes";

export const httpClient = axios.create({
  baseURL: import.meta.env.VITE_API_URL ?? "/api",
  headers: {
    "Content-Type": "application/json",
  },
});

const PUBLIC_AUTH_PATHS = [
  "/v1/auth/login",
  "/v1/auth/register",
  "/v1/auth/oauth2/exchange",
  "/v1/auth/verify-email",
  "/v1/auth/verify-email/resend",
  "/v1/auth/password/forgot",
  "/v1/auth/password/reset",
];

interface RetriableRequest extends InternalAxiosRequestConfig {
  _authRetry?: boolean;
}

let refreshRequest: Promise<string> | null = null;

httpClient.interceptors.request.use((config) => {
  const accessToken = getStoredSession()?.accessToken;
  if (accessToken && !isPublicAuthRequest(config.url)) {
    config.headers.Authorization = `Bearer ${accessToken}`;
  }
  return config;
});

httpClient.interceptors.response.use(
  (response) => response,
  async (error: AxiosError) => {
    const request = error.config as RetriableRequest | undefined;
    const session = getStoredSession();
    if (
      error.response?.status !== 401 ||
      !request ||
      request._authRetry ||
      isPublicAuthRequest(request.url) ||
      request.url?.endsWith("/v1/auth/refresh") ||
      !session?.refreshToken
    ) {
      throw error;
    }

    request._authRetry = true;
    refreshRequest ??= rotateRefreshToken(session.refreshToken);
    try {
      const accessToken = await refreshRequest;
      request.headers.Authorization = `Bearer ${accessToken}`;
      return await httpClient(request);
    } catch (refreshError) {
      clearStoredSession();
      throw refreshError;
    } finally {
      refreshRequest = null;
    }
  },
);

async function rotateRefreshToken(refreshToken: string): Promise<string> {
  const response = await axios.post<RefreshedTokens>(
    `${httpClient.defaults.baseURL}/v1/auth/refresh`,
    { refreshToken },
  );
  const session = getStoredSession();
  if (!session) {
    throw new Error("Authentication session is unavailable");
  }
  saveStoredSession({ ...session, ...response.data });
  return response.data.accessToken;
}

function isPublicAuthRequest(url: string | undefined): boolean {
  if (!url) {
    return false;
  }
  const path = url.startsWith("http") ? new URL(url).pathname : url.split("?")[0];
  return PUBLIC_AUTH_PATHS.includes(path);
}
