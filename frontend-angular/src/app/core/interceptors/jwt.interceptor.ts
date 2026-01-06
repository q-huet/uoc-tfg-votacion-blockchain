import { HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { AuthService } from '../services/auth.service';

/**
 * JWT Interceptor
 * 
 * Añade automáticamente el token JWT a todas las peticiones HTTP
 * excepto a los endpoints públicos (como /auth/login)
 * 
 * @author Enrique Huet Adrover
 */
export const jwtInterceptor: HttpInterceptorFn = (req, next) => {
  const authService = inject(AuthService);
  
  // Lista de URLs públicas que no requieren token
  const publicUrls = [
    '/auth/login',
    '/auth/register',
    '/public'
  ];
  
  // Verificar si la URL es pública
  const isPublicUrl = publicUrls.some(url => req.url.includes(url));
  
  // Si no es una URL pública y tenemos token, añadirlo
  if (!isPublicUrl) {
    const token = authService.getToken();
    
    if (token) {
      // Clonar la petición y añadir el header Authorization
      req = req.clone({
        setHeaders: {
          Authorization: `Bearer ${token}`
        }
      });
    }
  }
  
  return next(req);
};
