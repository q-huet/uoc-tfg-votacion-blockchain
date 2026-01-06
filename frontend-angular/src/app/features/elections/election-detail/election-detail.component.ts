import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { ElectionService } from '../../../core/services/election.service';
import { VoteService } from '../../../core/services/vote.service';
import { AuthService } from '../../../core/services/auth.service';
import { Election } from '../../../models/election.model';
import { ElectionOption } from '../../../models/election-option.model';

// PrimeNG imports
import { CardModule } from 'primeng/card';
import { ButtonModule } from 'primeng/button';
import { RadioButtonModule } from 'primeng/radiobutton';
import { SkeletonModule } from 'primeng/skeleton';
import { MessageModule } from 'primeng/message';
import { DividerModule } from 'primeng/divider';
import { ConfirmDialogModule } from 'primeng/confirmdialog';
import { DialogModule } from 'primeng/dialog';
import { ConfirmationService, MessageService } from 'primeng/api';
import { ToastModule } from 'primeng/toast';
import { ProgressBarModule } from 'primeng/progressbar';
import JSEncrypt from 'jsencrypt';

@Component({
  selector: 'app-election-detail',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    CardModule,
    ButtonModule,
    RadioButtonModule,
    SkeletonModule,
    MessageModule,
    DividerModule,
    ConfirmDialogModule,
    DialogModule,
    ToastModule,
    ProgressBarModule
  ],
  providers: [ConfirmationService, MessageService],
  templateUrl: './election-detail.component.html',
  styleUrl: './election-detail.component.scss'
})
export class ElectionDetailComponent implements OnInit {
  election: Election | null = null;
  selectedOptionId: string | null = null;
  loading = true;
  submitting = false;
  errorMessage = '';
  hasVoted = false;
  showVoteConfirmation = false;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private electionService: ElectionService,
    private voteService: VoteService,
    private authService: AuthService,
    private confirmationService: ConfirmationService,
    private messageService: MessageService
  ) {}

  ngOnInit(): void {
    const electionId = this.route.snapshot.paramMap.get('id');
    if (electionId) {
      this.loadElection(electionId);
      this.checkIfVoted(electionId);
    } else {
      this.errorMessage = 'ID de elección no válido';
      this.loading = false;
    }
  }

  /**
   * Cargar detalle de la elección
   */
  private loadElection(electionId: string): void {
    this.loading = true;
    this.errorMessage = '';

    this.electionService.getElectionById(electionId).subscribe({
      next: (election) => {
        this.election = election;
        this.checkIfVoted(electionId); // Re-check after loading election data
        this.loading = false;
      },
      error: (error) => {
        console.error('Error loading election:', error);
        this.errorMessage = 'Error al cargar la elección';
        this.loading = false;
      }
    });
  }

  /**
   * Verificar si el usuario ya votó
   */
  private checkIfVoted(electionId: string): void {
    // Prioritize server status, fallback to local receipt check
    if (this.election && this.election.hasVoted !== undefined) {
      this.hasVoted = this.election.hasVoted;
    } else {
      this.hasVoted = this.voteService.hasVoted(electionId);
    }
  }

  /**
   * Verificar si la elección está activa para votar
   */
  isActiveForVoting(): boolean {
    if (!this.election) return false;
    return this.electionService.isActiveForVoting(this.election);
  }

  /**
   * Verificar si el usuario puede votar
   */
  canVote(): boolean {
    return this.isActiveForVoting() && !this.hasVoted && this.authService.isAuthenticated();
  }

  /**
   * Verificar si el usuario es administrador
   */
  isAdmin(): boolean {
    return this.authService.hasRole('ADMIN');
  }

  /**
   * Seleccionar una opción
   */
  selectOption(optionId: string): void {
    if (this.canVote()) {
      this.selectedOptionId = optionId;
    }
  }

  /**
   * Confirmar y emitir voto
   */
  confirmVote(): void {
    if (!this.selectedOptionId || !this.election) {
      this.messageService.add({
        severity: 'warn',
        summary: 'Atención',
        detail: 'Por favor selecciona una opción antes de votar'
      });
      return;
    }

    const selectedOption = this.election.options.find(
      opt => opt.optionId === this.selectedOptionId
    );

    this.confirmationService.confirm({
      message: `¿Estás seguro de que quieres votar por "${selectedOption?.title}"? Esta acción no se puede deshacer.`,
      header: 'Confirmar Voto',
      icon: 'pi pi-exclamation-triangle',
      accept: () => {
        this.submitVote();
      }
    });
  }

  /**
   * Emitir voto
   */
  private submitVote(): void {
    if (!this.election || !this.selectedOptionId) return;

    this.submitting = true;
    this.errorMessage = '';

    let encryptedPayload: string | undefined;
    let optionIdToSend: string | undefined = this.selectedOptionId;

    // Si hay clave pública, ciframos el voto
    if (this.election.publicKey) {
        try {
            const encryptor = new JSEncrypt();
            encryptor.setPublicKey(this.election.publicKey);
            const encrypted = encryptor.encrypt(this.selectedOptionId);

            if (!encrypted) {
                throw new Error('Encryption failed');
            }

            encryptedPayload = encrypted;
            optionIdToSend = undefined; // No enviamos el ID en claro
            console.log('Vote encrypted successfully');
        } catch (e) {
            console.error('Encryption error:', e);
            this.messageService.add({
                severity: 'error',
                summary: 'Error de Cifrado',
                detail: 'No se pudo cifrar el voto. Inténtalo de nuevo.'
            });
            this.submitting = false;
            return;
        }
    }

    this.voteService.submitVote(
      this.election.id,
      optionIdToSend,
      encryptedPayload
    ).subscribe({
      next: (receipt) => {
        this.submitting = false;
        this.hasVoted = true;
        this.showVoteConfirmation = true;

        this.messageService.add({
          severity: 'success',
          summary: 'Voto Registrado',
          detail: 'Tu voto ha sido registrado exitosamente en la blockchain'
        });

        // Navegar a confirmación después de 2 segundos
        setTimeout(() => {
          this.router.navigate(['/vote-confirmation', this.election!.id], {
            state: { receipt }
          });
        }, 2000);
      },
      error: (error) => {
        console.error('Error submitting vote:', error);
        this.errorMessage = error.message || 'Error al emitir el voto';
        this.submitting = false;

        this.messageService.add({
          severity: 'error',
          summary: 'Error',
          detail: this.errorMessage
        });
      }
    });
  }

  /**
   * Obtener días restantes
   */
  getDaysRemaining(): number {
    if (!this.election) return 0;
    const endDate = new Date(this.election.endTime);
    const now = new Date();
    const diff = endDate.getTime() - now.getTime();
    return Math.ceil(diff / (1000 * 60 * 60 * 24));
  }

  /**
   * Obtener porcentaje de tiempo transcurrido
   */
  getTimeProgress(): number {
    if (!this.election) return 0;
    const startDate = new Date(this.election.startTime);
    const endDate = new Date(this.election.endTime);
    const now = new Date();

    const total = endDate.getTime() - startDate.getTime();
    const elapsed = now.getTime() - startDate.getTime();

    return Math.min(Math.max((elapsed / total) * 100, 0), 100);
  }

  /**
   * Verificar si está próxima a cerrar
   */
  isClosingSoon(): boolean {
    const daysRemaining = this.getDaysRemaining();
    return daysRemaining > 0 && daysRemaining <= 3 && this.isActiveForVoting();
  }

  /**
   * Obtener clase CSS para el estado
   */
  getStatusClass(): string {
    if (!this.election) return '';

    switch (this.election.status) {
      case 'active':
        return 'status-active';
      case 'draft':
        return 'status-draft';
      case 'closed':
      case 'completed':
        return 'status-closed';
      case 'cancelled':
        return 'status-cancelled';
      default:
        return '';
    }
  }

  /**
   * Obtener etiqueta del estado
   */
  getStatusLabel(): string {
    if (!this.election) return '';

    switch (this.election.status) {
      case 'active':
        return 'Activa';
      case 'draft':
        return 'Borrador';
      case 'closed':
        return 'Cerrada';
      case 'completed':
        return 'Completada';
      case 'cancelled':
        return 'Cancelada';
      default:
        return this.election.status;
    }
  }

  /**
   * Volver a la lista
   */
  goBack(): void {
    this.router.navigate(['/elections']);
  }

  /**
   * Ver recibo de voto
   */
  viewReceipt(): void {
    if (this.election && this.hasVoted) {
      this.router.navigate(['/vote-confirmation', this.election.id]);
    }
  }
}
