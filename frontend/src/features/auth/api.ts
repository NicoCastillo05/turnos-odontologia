import { http } from '../../shared/api/http';
import type { LoginResponse } from './types';

export interface LoginRequest {
  email: string;
  password: string;
}

export async function login(email: string, password: string): Promise<LoginResponse> {
  return http<LoginResponse>('/api/auth/login', {
    method: 'POST',
    body: JSON.stringify({ email, password } satisfies LoginRequest),
  });
}

export async function refresh(refreshToken: string): Promise<{ accessToken: string; refreshToken?: string }> {
  return http<{ accessToken: string; refreshToken?: string }>('/api/auth/refresh', {
    method: 'POST',
    body: JSON.stringify({ refreshToken }),
  });
}