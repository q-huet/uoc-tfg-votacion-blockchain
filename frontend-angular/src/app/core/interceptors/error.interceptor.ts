import { HttpInterceptorFn, HttpErrorResponse } from '@angular/common/http';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { catchError, throwError } from 'rxjs';
import { AuthService } from '../services/auth.service';

/**
 * Error Interceptor
 * 
 * Intercepta y maneja errores HTTP globalmente:
 * - 401 Unauthorized: Token expirado o inválido -> Redirige a login
 * - 403 Forbidden: Sin permisos -> Muestra mensaje
 * - 404 Not Found: Recurso no encontrado
 * - 500 Internal Server Error: Error del servidor
 * 
 * @author Enrique Huet Adrover
 */
export const errorInterceptor: HttpInterceptorFn = (req, next) => {
  const authService = inject(AuthService);
  const router = inject(Router);
  
  return next(req).pipe(
    catchError((error: HttpErrorResponse) => {
      let errorMessage = 'Ha ocurrido un error';
      
      if (error.error instanceof ErrorEvent) {
        // Error del lado del cliente
        errorMessage = `Error: ${error.error.message}`;
        console.error('Error del cliente:', error.error.message);
      } else {
        // Error del lado del servidor
        switch (error.status) {
          case 401:
            // Token expirado o inválido - Cerrar sesión y redirigir a login
            console.error('401 Unauthorized - Token inválido o expirado');
            errorMessage = 'Sesión expirada. Por favor, inicia sesión nuevamente.';
            authService.logout();
            router.navigate(['/login'], { 
              queryParams: { returnUrl: router.url, reason: 'session-expired' } 
            });
            break;
            
          case 403:
            // Sin permisos
            console.error('403 Forbidden - Sin permisos para acceder');
            errorMessage = 'No tienes permisos para realizar esta acción.';
            break;
            
          case 404:
            // Recurso no encontrado
            console.error('404 Not Found - Recurso no encontrado');
            errorMessage = 'Recurso no encontrado.';
            break;
            
          case 500:
            // Error interno del servidor
            console.error('500 Internal Server Error');
            errorMessage = 'Error interno del servidor. Intenta nuevamente más tarde.';
            break;
            
          case 0:
            // Sin conexión al servidor
            console.error('Error de conexión - No se puede conectar al servidor');
            errorMessage = 'No se puede conectar al servidor. Verifica tu conexión.';
            break;
            
          default:
            // Otros errores
            errorMessage = error.error?.message || `Error ${error.status}: ${error.statusText}`;
            console.error(`Error ${error.status}:`, errorMessage);
        }
      }
      
      // Retornar el error para que los componentes puedan manejarlo
      return throwError(() => ({
        status: error.status,
        message: errorMessage,
        error: error.error
      }));
    })
  );
};
