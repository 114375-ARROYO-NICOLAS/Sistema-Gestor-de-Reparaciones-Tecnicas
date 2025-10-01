export interface LoginRequest {
  username: string;
  password: string;
}

export interface UserInfo {
  id: number;
  username: string;
  nombreCompleto: string;
  email: string;
  rol: string;
  empleadoId: number;
  activo: boolean;
  ultimoLogin: string;
}

export interface LoginResponse {
  token: string;
  refreshToken: string;
  expiresIn: number;
  userInfo: UserInfo;
}

export interface RefreshTokenRequest {
  refreshToken: string;
}

export interface ApiError {
  error: string;
  message: string;
}

export interface ProfileResponse {
  id: number;
  username: string;
  nombreCompleto: string;
  email: string;
  rol: string;
  empleadoId: number;
  activo: boolean;
  ultimoLogin: string;
}

export interface TokenValidationResponse {
  valid: boolean;
  username: string;
  authorities: string[];
}
