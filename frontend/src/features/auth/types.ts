export interface LoginResponse {
  accessToken: string;
  refreshToken: string;
  role?: string;
}

export interface AuthTokens {
  accessToken: string;
  refreshToken: string;
}