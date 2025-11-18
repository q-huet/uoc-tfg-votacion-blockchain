import { UserRole } from './user-role.enum';

/**
 * Modelo de Usuario
 * Coincide con User.java (record) del backend
 */
export interface User {
  id: string;
  username: string;
  email: string;
  fullName: string;
  role: UserRole;
  department: string | null;
  active: boolean;
  lastLogin: string | null; // ISO 8601 datetime string
  createdAt: string; // ISO 8601 datetime string
}

/**
 * Información básica del usuario para la UI
 */
export interface UserInfo {
  id: string;
  username: string;
  email: string;
  fullName: string;
  role: string;
  department: string | null;
  lastLogin: string | null;
}
