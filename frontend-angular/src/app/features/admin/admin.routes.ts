import { Routes } from '@angular/router';

export const ADMIN_ROUTES: Routes = [
  {
    path: '',
    loadComponent: () =>
      import('./admin-dashboard/admin-dashboard.component').then(m => m.AdminDashboardComponent)
  },
  {
    path: 'create',
    loadComponent: () =>
      import('./create-election/create-election.component').then(m => m.CreateElectionComponent)
  },
  {
    path: 'results/:id',
    loadComponent: () =>
      import('./election-results/election-results.component').then(m => m.ElectionResultsComponent)
  }
];
