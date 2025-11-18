import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { ElectionService } from '../../../core/services/election.service';
import { ElectionSummary } from '../../../models/election.model';
import { ElectionStatus } from '../../../models/election-status.enum';

// PrimeNG imports
import { CardModule } from 'primeng/card';
import { ButtonModule } from 'primeng/button';
import { InputTextModule } from 'primeng/inputtext';
import { DropdownModule } from 'primeng/dropdown';
import { SkeletonModule } from 'primeng/skeleton';
import { MessageModule } from 'primeng/message';
import { BadgeModule } from 'primeng/badge';
import { DividerModule } from 'primeng/divider';

@Component({
  selector: 'app-election-list',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    CardModule,
    ButtonModule,
    InputTextModule,
    DropdownModule,
    SkeletonModule,
    MessageModule,
    BadgeModule,
    DividerModule
  ],
  templateUrl: './election-list.component.html',
  styleUrl: './election-list.component.scss'
})
export class ElectionListComponent implements OnInit {
  elections: ElectionSummary[] = [];
  filteredElections: ElectionSummary[] = [];
  loading = true;
  errorMessage = '';

  // Filtros
  searchTerm = '';
  selectedStatus: string | null = null;

  statusOptions = [
    { label: 'Todas', value: null },
    { label: 'Activas', value: 'ACTIVE' },
    { label: 'Borradores', value: 'DRAFT' },
    { label: 'Cerradas', value: 'CLOSED' },
    { label: 'Completadas', value: 'COMPLETED' },
    { label: 'Canceladas', value: 'CANCELLED' }
  ];

  constructor(
    private electionService: ElectionService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.loadElections();
  }

  /**
   * Cargar todas las elecciones
   */
  private loadElections(): void {
    this.loading = true;
    this.errorMessage = '';

    this.electionService.getActiveElections().subscribe({
      next: (elections) => {
        this.elections = elections;
        this.applyFilters();
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
   * Aplicar filtros de búsqueda y estado
   */
  applyFilters(): void {
    let filtered = [...this.elections];

    // Filtro por texto
    if (this.searchTerm) {
      const term = this.searchTerm.toLowerCase();
      filtered = filtered.filter(election =>
        election.title.toLowerCase().includes(term) ||
        election.description.toLowerCase().includes(term)
      );
    }

    // Filtro por estado
    if (this.selectedStatus) {
      filtered = filtered.filter(election => election.status === this.selectedStatus);
    }

    this.filteredElections = filtered;
  }

  /**
   * Limpiar todos los filtros
   */
  clearFilters(): void {
    this.searchTerm = '';
    this.selectedStatus = null;
    this.applyFilters();
  }

  /**
   * Navegar a detalle de elección
   */
  viewElection(electionId: string): void {
    this.router.navigate(['/elections', electionId]);
  }

  /**
   * Refrescar lista de elecciones
   */
  refresh(): void {
    this.loadElections();
  }

  /**
   * Obtener clase CSS para badge de estado
   */
  getStatusClass(status: string): string {
    switch (status) {
      case 'ACTIVE':
        return 'status-badge-active';
      case 'DRAFT':
        return 'status-badge-draft';
      case 'CLOSED':
      case 'COMPLETED':
        return 'status-badge-closed';
      case 'CANCELLED':
        return 'status-badge-cancelled';
      default:
        return '';
    }
  }

  /**
   * Obtener texto amigable para el estado
   */
  getStatusLabel(status: string): string {
    switch (status) {
      case 'ACTIVE':
        return 'Activa';
      case 'DRAFT':
        return 'Borrador';
      case 'CLOSED':
        return 'Cerrada';
      case 'COMPLETED':
        return 'Completada';
      case 'CANCELLED':
        return 'Cancelada';
      default:
        return status;
    }
  }

  /**
   * Verificar si puede votar en una elección
   */
  canVote(election: ElectionSummary): boolean {
    return election.canVote && !election.hasVoted;
  }

  /**
   * Obtener días restantes para votar
   */
  getDaysRemaining(election: ElectionSummary): number {
    const endDate = new Date(election.endTime);
    const now = new Date();
    const diff = endDate.getTime() - now.getTime();
    return Math.ceil(diff / (1000 * 60 * 60 * 24));
  }

  /**
   * Verificar si está próxima a cerrar (menos de 3 días)
   */
  isClosingSoon(election: ElectionSummary): boolean {
    const daysRemaining = this.getDaysRemaining(election);
    return daysRemaining > 0 && daysRemaining <= 3 && election.status === 'ACTIVE';
  }
}
