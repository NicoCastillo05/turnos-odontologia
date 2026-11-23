const API_BASE = import.meta.env.VITE_API_BASE ?? '';

const ACCESS_KEY = 'accessToken';
const REFRESH_KEY = 'refreshToken';

export function getAccessToken(): string | null {
  return localStorage.getItem(ACCESS_KEY);
}

export function getRefreshToken(): string | null {
  return localStorage.getItem(REFRESH_KEY);
}

export function setTokens(access: string, refresh: string): void {
  localStorage.setItem(ACCESS_KEY, access);
  localStorage.setItem(REFRESH_KEY, refresh);
}

export function clearTokens(): void {
  localStorage.removeItem(ACCESS_KEY);
  localStorage.removeItem(REFRESH_KEY);
}

let refreshPromise: Promise<string | null> | null = null;

async function refreshAccessToken(): Promise<string | null> {
  const rt = getRefreshToken();
  if (rt === null) return null;
  if (refreshPromise !== null) return refreshPromise;

  refreshPromise = (async (): Promise<string | null> => {
    try {
      const res = await fetch(`${API_BASE}/api/auth/refresh`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ refreshToken: rt }),
      });
      if (!res.ok) {
        clearTokens();
        return null;
      }
      const data = (await res.json()) as { accessToken: string; refreshToken?: string };
      localStorage.setItem(ACCESS_KEY, data.accessToken);
      if (data.refreshToken !== undefined) {
        localStorage.setItem(REFRESH_KEY, data.refreshToken);
      }
      return data.accessToken;
    } catch {
      clearTokens();
      return null;
    } finally {
      refreshPromise = null;
    }
  })();

  return refreshPromise;
}

export async function http<T>(path: string, options: RequestInit = {}): Promise<T> {
  const doFetch = async (token: string | null): Promise<Response> => {
    const headers = new Headers(options.headers);
    if (token !== null) {
      headers.set('Authorization', `Bearer ${token}`);
    }
    if (options.body !== undefined && !headers.has('Content-Type')) {
      headers.set('Content-Type', 'application/json');
    }
    return fetch(`${API_BASE}${path}`, { ...options, headers });
  };

  let token = getAccessToken();
  let res = await doFetch(token);
  if (res.status === 401) {
    const newToken = await refreshAccessToken();
    if (newToken !== null) {
      res = await doFetch(newToken);
    }
  }
  if (!res.ok) {
    const text = await res.text();
    throw new Error(text || `HTTP ${res.status}`);
  }
  if (res.status === 204) {
    return undefined as T;
  }
  return (await res.json()) as T;
}