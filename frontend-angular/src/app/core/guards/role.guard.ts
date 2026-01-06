import { inject } from '@angular/core';
import { Router, CanActivateFn, ActivatedRouteSnapshot } from '@angular/router';
import { AuthService } from '@core/services/auth.service';
import { UserRole } from '@models/user-role.enum';

/**
 * Guard para proteger rutas basándose en roles de usuario.
 * Verifica que el usuario tenga al menos uno de los roles requeridos.
 *
 * Uso en rutas:
 * {
 *   path: 'admin',
 *   component: AdminComponent,
 *   canActivate: [roleGuard],
 *   data: { roles: [UserRole.ADMIN] }
 * }
 */
export const roleGuard: CanActivateFn = (route: ActivatedRouteSnapshot) => {
  const authService = inject(AuthService);
  const router = inject(Router);

  // Verificar autenticación primero
  if (!authService.isAuthenticated()) {
    router.navigate(['/auth/login']);
    return false;
  }

  // Obtener roles requeridos de la configuración de la ruta
  const requiredRoles = route.data['roles'] as UserRole[];

  if (!requiredRoles || requiredRoles.length === 0) {
    // Si no se especifican roles, permitir acceso a usuarios autenticados
    return true;
  }

  // Verificar si el usuario tiene alguno de los roles requeridos
  if (authService.hasAnyRole(requiredRoles)) {
    return true;
  }

  // Usuario no tiene los roles necesarios
  router.navigate(['/unauthorized']);
  return false;
};
