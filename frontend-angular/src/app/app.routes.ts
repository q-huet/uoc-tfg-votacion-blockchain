import { Routes } from '@angular/router';
import { authGuard } from './core/guards/auth.guard';
import { roleGuard } from './core/guards/role.guard';
import { UserRole } from './models/user-role.enum';

export const routes: Routes = [
  // Redirección inicial
  {
    path: '',
    redirectTo: '/dashboard',
    pathMatch: 'full'
  },

  // Rutas de autenticación (públicas)
  {
    path: 'auth',
    children: [
      {
        path: 'login',
        loadComponent: () => import('./features/auth/login/login.component').then(m => m.LoginComponent)
      }
    ]
  },

  // Dashboard (requiere autenticación)
  {
    path: 'dashboard',
    loadComponent: () => import('./features/dashboard/dashboard.component').then(m => m.DashboardComponent),
    canActivate: [authGuard]
  },

  // Elecciones (requiere autenticación)
  {
    path: 'elections',
    canActivate: [authGuard],
    children: [
      {
        path: '',
        loadComponent: () => import('./features/elections/election-list/election-list.component').then(m => m.ElectionListComponent)
      },
      {
        path: ':id',
        loadComponent: () => import('./features/elections/election-detail/election-detail.component').then(m => m.ElectionDetailComponent)
      }
    ]
  },

  // Confirmación de voto (requiere autenticación)
  {
    path: 'vote-confirmation/:id',
    loadComponent: () => import('./features/vote-confirmation/vote-confirmation.component').then(m => m.VoteConfirmationComponent),
    canActivate: [authGuard]
  },

  // Recibos (requiere autenticación)
  {
    path: 'receipts',
    loadComponent: () => import('./features/vote-confirmation/vote-confirmation.component').then(m => m.VoteConfirmationComponent),
    canActivate: [authGuard]
  },

  // Panel de administración (solo ADMIN)
  {
    path: 'admin',
    canActivate: [authGuard, roleGuard],
    data: { roles: [UserRole.ADMIN] },
    loadChildren: () => import('./features/admin/admin.routes').then(m => m.ADMIN_ROUTES)
  },

  // Vista de auditoría (solo AUDITOR)
  {
    path: 'audit',
    canActivate: [authGuard, roleGuard],
    data: { roles: [UserRole.AUDITOR, UserRole.ADMIN] },
    loadChildren: () => import('./features/audit/audit.routes').then(m => m.AUDIT_ROUTES)
  },

  // Página de no autorizado
  {
    path: 'unauthorized',
    loadComponent: () => import('./shared/components/unauthorized/unauthorized.component').then(m => m.UnauthorizedComponent)
  },

  // Página de no encontrado
  {
    path: 'not-found',
    loadComponent: () => import('./shared/components/not-found/not-found.component').then(m => m.NotFoundComponent)
  },

  // Redirección de rutas no encontradas
  {
    path: '**',
    redirectTo: '/not-found'
  }
];
