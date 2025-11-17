import { Routes } from '@angular/router';
import { LoginComponent } from './features/auth/login/login.component';

export const routes: Routes = [
  { path: '', redirectTo: '/login', pathMatch: 'full' },
  { path: 'login', component: LoginComponent },
  // Rutas futuras:
  // { path: 'elections', component: ElectionsComponent, canActivate: [AuthGuard] },
  // { path: 'vote/:id', component: VotingComponent, canActivate: [AuthGuard] },
  { path: '**', redirectTo: '/login' }
];
