import { Navigate } from 'react-router-dom';
import type { ReactNode } from 'react';
import { useAuth } from '../../features/auth/AuthContext';

export default function RequireAuth({ children }: { children: ReactNode }): JSX.Element {
  const { accessToken } = useAuth();
  if (accessToken === null) {
    return <Navigate to="/login" replace />;
  }
  return <>{children}</>;
}