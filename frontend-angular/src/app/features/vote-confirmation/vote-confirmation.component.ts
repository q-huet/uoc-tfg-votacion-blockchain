import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router';
import { VoteService } from '../../core/services/vote.service';
import { ElectionService } from '../../core/services/election.service';
import { VoteReceipt } from '../../models/vote.model';
import { Election } from '../../models/election.model';

// PrimeNG imports
import { CardModule } from 'primeng/card';
import { ButtonModule } from 'primeng/button';
import { DividerModule } from 'primeng/divider';
import { MessageModule } from 'primeng/message';
import { MessageService } from 'primeng/api';
import { ToastModule } from 'primeng/toast';

@Component({
  selector: 'app-vote-confirmation',
  standalone: true,
  imports: [
    CommonModule,
    CardModule,
    ButtonModule,
    DividerModule,
    MessageModule,
    ToastModule
  ],
  providers: [MessageService],
  templateUrl: './vote-confirmation.component.html',
  styleUrl: './vote-confirmation.component.scss'
})
export class VoteConfirmationComponent implements OnInit {
  receipt: VoteReceipt | null = null;
  election: Election | null = null;
  loading = true;
  errorMessage = '';

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private voteService: VoteService,
    private electionService: ElectionService,
    private messageService: MessageService
  ) {
    // Intentar obtener receipt del state de navegación
    const navigation = this.router.getCurrentNavigation();
    if (navigation?.extras?.state) {
      this.receipt = navigation.extras.state['receipt'];
    }
  }

  ngOnInit(): void {
    const electionId = this.route.snapshot.paramMap.get('id');

    if (!electionId) {
      this.errorMessage = 'ID de elección no válido';
      this.loading = false;
      return;
    }

    // Si no hay receipt en el state, intentar obtenerlo del cache
    if (!this.receipt) {
      this.receipt = this.voteService.getVoteReceipt(electionId);

      if (!this.receipt) {
        this.errorMessage = 'No se encontró el recibo de voto';
        this.loading = false;
        return;
      }
    }

    // Cargar información de la elección
    this.loadElection(electionId);
  }

  /**
   * Cargar información de la elección
   */
  private loadElection(electionId: string): void {
    this.loading = true;

    this.electionService.getElectionById(electionId).subscribe({
      next: (election) => {
        this.election = election;
        this.loading = false;
      },
      error: (error) => {
        console.error('Error loading election:', error);
        this.errorMessage = 'Error al cargar la información de la elección';
        this.loading = false;
      }
    });
  }

  /**
   * Copiar texto al portapapeles
   */
  copyToClipboard(text: string, label: string): void {
    navigator.clipboard.writeText(text).then(() => {
      this.messageService.add({
        severity: 'success',
        summary: 'Copiado',
        detail: `${label} copiado al portapapeles`
      });
    }).catch(() => {
      this.messageService.add({
        severity: 'error',
        summary: 'Error',
        detail: 'No se pudo copiar al portapapeles'
      });
    });
  }

  /**
   * Descargar recibo como texto
   */
  downloadReceipt(): void {
    if (!this.receipt) return;

    const receiptText = this.voteService.exportReceiptAsText(this.receipt);
    const blob = new Blob([receiptText], { type: 'text/plain' });
    const url = window.URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.href = url;
    link.download = `recibo-voto-${this.receipt.receiptId}.txt`;
    link.click();
    window.URL.revokeObjectURL(url);

    this.messageService.add({
      severity: 'success',
      summary: 'Descargado',
      detail: 'Recibo descargado correctamente'
    });
  }

  /**
   * Imprimir recibo
   */
  printReceipt(): void {
    window.print();
  }

  /**
   * Verificar recibo en blockchain
   */
  verifyReceipt(): void {
    if (!this.receipt) return;

    this.messageService.add({
      severity: 'info',
      summary: 'Verificando',
      detail: 'Verificando recibo en blockchain...'
    });

    this.voteService.verifyReceipt(this.receipt).subscribe({
      next: (isValid) => {
        if (isValid) {
          this.messageService.add({
            severity: 'success',
            summary: 'Verificado',
            detail: 'El recibo es válido y está registrado en blockchain'
          });
        } else {
          this.messageService.add({
            severity: 'error',
            summary: 'Error',
            detail: 'No se pudo verificar el recibo'
          });
        }
      },
      error: (error) => {
        this.messageService.add({
          severity: 'error',
          summary: 'Error',
          detail: error.message || 'Error al verificar el recibo'
        });
      }
    });
  }

  /**
   * Volver al dashboard
   */
  goToDashboard(): void {
    this.router.navigate(['/dashboard']);
  }

  /**
   * Ver lista de elecciones
   */
  goToElections(): void {
    this.router.navigate(['/elections']);
  }

  /**
   * Obtener texto corto del hash
   */
  getShortHash(hash: string): string {
    if (!hash) return '';
    return `${hash.substring(0, 10)}...${hash.substring(hash.length - 10)}`;
  }
}
