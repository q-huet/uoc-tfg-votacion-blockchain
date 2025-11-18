import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { BehaviorSubject, Observable, throwError } from 'rxjs';
import { tap, catchError } from 'rxjs/operators';
import { environment } from '../../../environments/environment';
import {
  LoginRequest,
  LoginResponse,
  TokenValidationResponse,
  LogoutResponse,
  UserInfo
} from '../../models';

/**
 * Servicio de Autenticación
 *
 * Gestiona:
 * - Login y logout de usuarios
 * - Almacenamiento y validación de tokens JWT
 * - Estado de autenticación global
 * - Información del usuario actual
 *
 * @author TFG Votación Blockchain
 */
@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private readonly API_URL = environment.apiUrl;
  private readonly TOKEN_KEY = environment.jwt.tokenKey;

  // Estado de autenticación
  private currentUserSubject: BehaviorSubject<UserInfo | null>;
  public currentUser$: Observable<UserInfo | null>;

  // Estado de loading
  private loadingSubject: BehaviorSubject<boolean>;
  public loading$: Observable<boolean>;

  constructor(private http: HttpClient) {
    // Inicializar estado de usuario desde localStorage
    const storedUser = this.getStoredUser();
    this.currentUserSubject = new BehaviorSubject<UserInfo | null>(storedUser);
    this.currentUser$ = this.currentUserSubject.asObservable();

    // Inicializar estado de loading
    this.loadingSubject = new BehaviorSubject<boolean>(false);
    this.loading$ = this.loadingSubject.asObservable();
  }

  /**
   * Obtiene el usuario actual del estado
   */
  public get currentUserValue(): UserInfo | null {
    return this.currentUserSubject.value;
  }

  /**
   * Login de usuario
   * @param username Nombre de usuario
   * @param password Contraseña
   * @returns Observable con la respuesta de login
   */
  login(username: string, password: string): Observable<LoginResponse> {
    this.setLoading(true);

    const loginRequest: LoginRequest = { username, password };
    const url = `${this.API_URL}${environment.endpoints.auth.login}`;

    return this.http.post<LoginResponse>(url, loginRequest).pipe(
      tap(response => {
        // Guardar token y usuario
        this.saveToken(response.token);
        this.saveUser(response.user);

        // Actualizar estado
        this.currentUserSubject.next(response.user);

        console.log(`[AuthService] Usuario autenticado: ${response.user.username}`);
      }),
      catchError(error => {
        console.error('[AuthService] Error en login:', error);
        return throwError(() => error);
      }),
      tap(() => this.setLoading(false))
    );
  }

  /**
   * Logout de usuario
   * @returns Observable con la respuesta de logout
   */
  logout(): Observable<LogoutResponse> {
    const url = `${this.API_URL}${environment.endpoints.auth.logout}`;
    const token = this.getToken();

    if (!token) {
      // Si no hay token, solo limpiar localmente
      this.clearAuthData();
      return throwError(() => new Error('No hay sesión activa'));
    }

    const headers = new HttpHeaders().set(
      environment.jwt.headerName,
      `${environment.jwt.tokenPrefix} ${token}`
    );

    return this.http.post<LogoutResponse>(url, {}, { headers }).pipe(
      tap(response => {
        console.log('[AuthService] Logout exitoso:', response.message);
        this.clearAuthData();
      }),
      catchError(error => {
        console.error('[AuthService] Error en logout:', error);
        // Limpiar datos locales incluso si falla el logout en el servidor
        this.clearAuthData();
        return throwError(() => error);
      })
    );
  }

  /**
   * Valida el token JWT actual
   * @returns Observable con la respuesta de validación
   */
  validateToken(): Observable<TokenValidationResponse> {
    const token = this.getToken();

    if (!token) {
      return throwError(() => new Error('No hay token para validar'));
    }

    const url = `${this.API_URL}${environment.endpoints.auth.validate}`;
    const headers = new HttpHeaders().set(
      environment.jwt.headerName,
      `${environment.jwt.tokenPrefix} ${token}`
    );

    return this.http.get<TokenValidationResponse>(url, { headers }).pipe(
      tap(response => {
        if (!response.valid) {
          console.warn('[AuthService] Token inválido');
          this.clearAuthData();
        }
      }),
      catchError(error => {
        console.error('[AuthService] Error validando token:', error);
        this.clearAuthData();
        return throwError(() => error);
      })
    );
  }

  /**
   * Obtiene la información del usuario actual del servidor
   * @returns Observable con la información del usuario
   */
  getCurrentUser(): Observable<UserInfo> {
    const url = `${this.API_URL}${environment.endpoints.auth.user}`;
    const token = this.getToken();

    if (!token) {
      return throwError(() => new Error('No hay token de autenticación'));
    }

    const headers = new HttpHeaders().set(
      environment.jwt.headerName,
      `${environment.jwt.tokenPrefix} ${token}`
    );

    return this.http.get<UserInfo>(url, { headers }).pipe(
      tap(user => {
        this.saveUser(user);
        this.currentUserSubject.next(user);
      }),
      catchError(error => {
        console.error('[AuthService] Error obteniendo usuario:', error);
        return throwError(() => error);
      })
    );
  }

  /**
   * Verifica si el usuario está autenticado
   * @returns true si hay un token válido
   */
  isAuthenticated(): boolean {
    const token = this.getToken();
    return !!token && !this.isTokenExpired(token);
  }

  /**
   * Verifica si el usuario tiene un rol específico
   * @param role Rol a verificar
   * @returns true si el usuario tiene el rol
   */
  hasRole(role: string): boolean {
    const user = this.currentUserValue;
    return user?.role === role;
  }

  /**
   * Verifica si el usuario tiene alguno de los roles especificados
   * @param roles Array de roles a verificar
   * @returns true si el usuario tiene alguno de los roles
   */
  hasAnyRole(roles: string[]): boolean {
    const user = this.currentUserValue;
    return user ? roles.includes(user.role) : false;
  }

  // ==================== Gestión de Token ====================

  /**
   * Guarda el token JWT en localStorage
   * @param token Token JWT
   */
  saveToken(token: string): void {
    localStorage.setItem(this.TOKEN_KEY, token);
  }

  /**
   * Obtiene el token JWT de localStorage
   * @returns Token JWT o null si no existe
   */
  getToken(): string | null {
    return localStorage.getItem(this.TOKEN_KEY);
  }

  /**
   * Elimina el token JWT de localStorage
   */
  removeToken(): void {
    localStorage.removeItem(this.TOKEN_KEY);
  }

  /**
   * Verifica si el token ha expirado
   * @param token Token JWT
   * @returns true si el token ha expirado
   */
  private isTokenExpired(token: string): boolean {
    try {
      const payload = this.decodeToken(token);
      if (!payload.exp) {
        return false; // Si no tiene exp, considerarlo válido
      }

      const expirationDate = new Date(payload.exp * 1000);
      return expirationDate < new Date();
    } catch (error) {
      console.error('[AuthService] Error verificando expiración del token:', error);
      return true; // Si hay error al decodificar, considerar expirado
    }
  }

  /**
   * Decodifica el payload del token JWT
   * @param token Token JWT
   * @returns Payload decodificado
   */
  private decodeToken(token: string): any {
    try {
      const parts = token.split('.');
      if (parts.length !== 3) {
        throw new Error('Token JWT inválido');
      }

      const payload = parts[1];
      const decoded = atob(payload);
      return JSON.parse(decoded);
    } catch (error) {
      console.error('[AuthService] Error decodificando token:', error);
      throw error;
    }
  }

  // ==================== Gestión de Usuario ====================

  /**
   * Guarda la información del usuario en localStorage
   * @param user Información del usuario
   */
  private saveUser(user: UserInfo): void {
    localStorage.setItem('currentUser', JSON.stringify(user));
  }

  /**
   * Obtiene la información del usuario de localStorage
   * @returns Información del usuario o null
   */
  private getStoredUser(): UserInfo | null {
    const userJson = localStorage.getItem('currentUser');
    if (userJson) {
      try {
        return JSON.parse(userJson);
      } catch (error) {
        console.error('[AuthService] Error parseando usuario almacenado:', error);
        return null;
      }
    }
    return null;
  }

  /**
   * Elimina la información del usuario de localStorage
   */
  private removeUser(): void {
    localStorage.removeItem('currentUser');
  }

  /**
   * Limpia todos los datos de autenticación
   */
  private clearAuthData(): void {
    this.removeToken();
    this.removeUser();
    this.currentUserSubject.next(null);
    console.log('[AuthService] Datos de autenticación limpiados');
  }

  // ==================== Utilidades ====================

  /**
   * Establece el estado de loading
   * @param loading Estado de loading
   */
  private setLoading(loading: boolean): void {
    this.loadingSubject.next(loading);
  }

  /**
   * Obtiene el nombre completo del usuario actual
   * @returns Nombre completo o 'Usuario'
   */
  getUserFullName(): string {
    return this.currentUserValue?.fullName || 'Usuario';
  }

  /**
   * Obtiene el rol del usuario actual
   * @returns Rol del usuario o null
   */
  getUserRole(): string | null {
    return this.currentUserValue?.role || null;
  }

  /**
   * Obtiene el username del usuario actual
   * @returns Username o null
   */
  getUsername(): string | null {
    return this.currentUserValue?.username || null;
  }
}
