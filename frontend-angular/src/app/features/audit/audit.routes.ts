import { Routes } from '@angular/router';

export const AUDIT_ROUTES: Routes = [
  {
    path: '',
    loadComponent: () =>
      import('./audit-dashboard/audit-dashboard.component').then(m => m.AuditDashboardComponent)
  }
];
