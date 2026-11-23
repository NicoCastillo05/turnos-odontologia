import { createContext, useCallback, useContext, useMemo, useState } from 'react';
import type { ReactNode } from 'react';
import { clearTokens, getAccessToken, setTokens } from '../../shared/api/http';
import * as authApi from './api';

interface AuthContextValue {
  accessToken: string | null;
  login: (email: string, password: string) => Promise<void>;
  logout: () => void;
}

const AuthContext = createContext<AuthContextValue | null>(null);

export function AuthProvider({ children }: { children: ReactNode }): JSX.Element {
  const [accessToken, setAccessToken] = useState<string | null>(() => getAccessToken());

  const login = useCallback(async (email: string, password: string): Promise<void> => {
    const res = await authApi.login(email, password);
    setTokens(res.accessToken, res.refreshToken);
    setAccessToken(res.accessToken);
  }, []);

  const logout = useCallback((): void => {
    clearTokens();
    setAccessToken(null);
  }, []);

  const value = useMemo<AuthContextValue>(
    () => ({ accessToken, login, logout }),
    [accessToken, login, logout],
  );

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth(): AuthContextValue {
  const ctx = useContext(AuthContext);
  if (ctx === null) {
    throw new Error('useAuth must be used within AuthProvider');
  }
  return ctx;
}