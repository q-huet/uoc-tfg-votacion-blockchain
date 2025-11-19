import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { AuthService } from '../../core/services/auth.service';
import { ElectionService } from '../../core/services/election.service';
import { VoteService } from '../../core/services/vote.service';
import { User, UserRole, UserInfo } from '../../models/user.model';
import { ElectionSummary } from '../../models/election.model';

// PrimeNG imports
import { CardModule } from 'primeng/card';
import { ButtonModule } from 'primeng/button';
import { BadgeModule } from 'primeng/badge';
import { SkeletonModule } from 'primeng/skeleton';
import { MessageModule } from 'primeng/message';
import { DividerModule } from 'primeng/divider';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [
    CommonModule,
    CardModule,
    ButtonModule,
    BadgeModule,
    SkeletonModule,
    MessageModule,
    DividerModule
  ],
  templateUrl: './dashboard.component.html',
  styleUrl: './dashboard.component.scss'
})
export class DashboardComponent implements OnInit {
  currentUser: UserInfo | null = null;
  activeElections: ElectionSummary[] = [];
  loading = true;
  errorMessage = '';

  // Estadísticas
  stats = {
    totalActive: 0,
    voted: 0,
    pending: 0,
    receipts: 0
  };

  constructor(
    private authService: AuthService,
    private electionService: ElectionService,
    private voteService: VoteService,
    public router: Router
  ) {}

  ngOnInit(): void {
    this.loadUserData();
    this.loadElections();
  }

  /**
   * Cargar datos del usuario actual
   */
  private loadUserData(): void {
    this.authService.currentUser$.subscribe(user => {
      this.currentUser = user;
    });
  }

  /**
   * Cargar elecciones activas
   */
  private loadElections(): void {
    this.loading = true;
    this.errorMessage = '';

    this.electionService.getActiveElections().subscribe({
      next: (elections) => {
        this.activeElections = elections;
        this.calculateStats(elections);
        this.loading = false;
      },
      error: (error) => {
        console.error('Error loading elections:', error);
        this.errorMessage = 'Error al cargar las elecciones';
        this.loading = false;
      }
    });
  }

  /**
   * Calcular estadísticas del dashboard
   */
  private calculateStats(elections: ElectionSummary[]): void {
    this.stats.totalActive = elections.filter(e => e.status === 'ACTIVE').length;
    this.stats.voted = elections.filter(e => e.hasVoted).length;
    this.stats.pending = elections.filter(e => !e.hasVoted && e.canVote).length;
    this.stats.receipts = this.voteService.getAllReceipts().length;
  }

  /**
   * Verificar si el usuario es administrador
   */
  isAdmin(): boolean {
    return this.authService.hasRole(UserRole.ADMIN);
  }

  /**
   * Verificar si el usuario es auditor
   */
  isAuditor(): boolean {
    return this.authService.hasRole(UserRole.AUDITOR);
  }

  /**
   * Navegar a la lista de elecciones
   */
  goToElections(): void {
    this.router.navigate(['/elections']);
  }

  /**
   * Navegar al panel de administración
   */
  goToAdmin(): void {
    this.router.navigate(['/admin']);
  }

  /**
   * Navegar a la vista de auditoría
   */
  goToAudit(): void {
    this.router.navigate(['/audit']);
  }

  /**
   * Ver recibos de votos
   */
  viewReceipts(): void {
    this.router.navigate(['/receipts']);
  }

  /**
   * Cerrar sesión
   */
  logout(): void {
    this.authService.logout().subscribe({
      next: () => {
        console.log('Sesión cerrada correctamente');
        this.router.navigate(['/auth/login']);
      },
      error: (error) => {
        console.error('Error al cerrar sesión:', error);
        // Redirigir al login incluso si hay error (los datos locales ya se limpiaron)
        this.router.navigate(['/auth/login']);
      }
    });
  }

  /**
   * Refrescar datos del dashboard
   */
  refresh(): void {
    this.loadElections();
  }
}
