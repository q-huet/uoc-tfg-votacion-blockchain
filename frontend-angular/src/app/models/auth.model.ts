import { UserInfo } from './user.model';

/**
 * Request de login
 * Coincide con LoginRequest DTO del backend
 */
export interface LoginRequest {
  username: string;
  password: string;
}

/**
 * Response de login
 * Coincide con LoginResponse DTO del backend
 */
export interface LoginResponse {
  token: string;
  tokenType: string;
  expiresIn: number;
  user: UserInfo;
}

/**
 * Response de validación de token
 * Coincide con TokenValidationResponse DTO del backend
 */
export interface TokenValidationResponse {
  valid: boolean;
  username?: string;
  email?: string;
  role?: string;
  message: string;
}

/**
 * Response de logout
 */
export interface LogoutResponse {
  message: string;
  instruction: string;
}
