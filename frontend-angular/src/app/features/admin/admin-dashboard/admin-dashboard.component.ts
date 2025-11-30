import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterModule } from '@angular/router';
import { CardModule } from 'primeng/card';
import { ButtonModule } from 'primeng/button';
import { TableModule } from 'primeng/table';
import { TagModule } from 'primeng/tag';
import { ToastModule } from 'primeng/toast';
import { MessageService, ConfirmationService } from 'primeng/api';
import { ConfirmDialogModule } from 'primeng/confirmdialog';
import { ElectionService } from '../../../core/services/election.service';
import { ElectionSummary } from '../../../models/election.model';

@Component({
  selector: 'app-admin-dashboard',
  standalone: true,
  imports: [
    CommonModule,
    RouterModule,
    CardModule,
    ButtonModule,
    TableModule,
    TagModule,
    ToastModule,
    ConfirmDialogModule
  ],
  providers: [MessageService, ConfirmationService],
  templateUrl: './admin-dashboard.component.html',
  styleUrl: './admin-dashboard.component.scss'
})
export class AdminDashboardComponent implements OnInit {
  elections: ElectionSummary[] = [];
  loading: boolean = false;

  constructor(
    private router: Router,
    private electionService: ElectionService,
    private messageService: MessageService,
    private confirmationService: ConfirmationService
  ) {}

  ngOnInit() {
    this.loadElections();
  }

  loadElections() {
    this.loading = true;
    this.electionService.getAllElections().subscribe({
      next: (data) => {
        this.elections = data;
        this.loading = false;
      },
      error: (err) => {
        this.loading = false;
        this.messageService.add({severity:'error', summary:'Error', detail:'No se pudieron cargar las elecciones'});
      }
    });
  }

  navigateToCreate() {
    this.router.navigate(['/admin/create']);
  }

  viewResults(electionId: string) {
    this.router.navigate(['/admin/results', electionId]);
  }

  closeElection(election: ElectionSummary) {
    this.confirmationService.confirm({
      message: `¿Estás seguro de que quieres cerrar la elección "${election.title}"? Esta acción no se puede deshacer.`,
      header: 'Confirmar Cierre',
      icon: 'pi pi-exclamation-triangle',
      accept: () => {
        this.electionService.closeElection(election.electionId).subscribe({
          next: () => {
            this.messageService.add({severity:'success', summary:'Éxito', detail:'Elección cerrada correctamente'});
            this.loadElections();
          },
          error: (err) => {
            this.messageService.add({severity:'error', summary:'Error', detail:'No se pudo cerrar la elección'});
          }
        });
      }
    });
  }

  getSeverity(status: string): "success" | "info" | "warning" | "danger" | "secondary" | "contrast" | undefined {
    switch (status) {
      case 'active':
        return 'success';
      case 'closed':
        return 'danger';
      case 'draft':
        return 'warning';
      default:
        return 'info';
    }
  }
}
