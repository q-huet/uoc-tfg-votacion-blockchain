import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, throwError, BehaviorSubject } from 'rxjs';
import { map, catchError, tap } from 'rxjs/operators';
import { environment } from '@environments/environment';
import { VoteSubmissionRequest, VoteReceipt } from '@models/vote.model';
import { ErrorResponse } from '@models/error.model';

/**
 * Servicio para gestión de votos
 *
 * Funcionalidades:
 * - Emitir voto en una elección
 * - Obtener recibo de voto
 * - Verificar estado de voto
 * - Validar recibo de voto con blockchain
 */
@Injectable({
  providedIn: 'root'
})
export class VoteService {
  private readonly baseUrl = `${environment.apiUrl}${environment.endpoints.elections.base}`;

  // Cache de recibos de voto del usuario
  private voteReceiptsSubject = new BehaviorSubject<Map<string, VoteReceipt>>(new Map());
  public voteReceipts$ = this.voteReceiptsSubject.asObservable();

  // Estado de carga
  private loadingSubject = new BehaviorSubject<boolean>(false);
  public loading$ = this.loadingSubject.asObservable();

  constructor(private http: HttpClient) {
    this.loadReceiptsFromStorage();
  }

  /**
   * Emitir un voto en una elección
   * POST /api/v1/elections/{id}/vote
   *
   * @param electionId ID de la elección
   * @param optionId ID de la opción seleccionada
   * @param comment Comentario opcional (cifrado)
   * @returns Observable con el recibo del voto
   */
  submitVote(
    electionId: string,
    optionId: string,
    comment?: string
  ): Observable<VoteReceipt> {
    this.loadingSubject.next(true);

    const url = `${this.baseUrl}/${electionId}/vote`;

    const request: VoteSubmissionRequest = {
      optionId,
      voterComments: comment
    };

    return this.http.post<any>(url, request).pipe(
      map(response => this.mapToVoteReceipt(response, electionId)),
      tap(receipt => {
        this.loadingSubject.next(false);
        this.storeReceipt(electionId, receipt);
      }),
      catchError(error => {
        this.loadingSubject.next(false);
        return this.handleError(error);
      })
    );
  }

  /**
   * Obtener recibo de voto para una elección
   * Recupera del cache local
   *
   * @param electionId ID de la elección
   * @returns Recibo de voto o null si no existe
   */
  getVoteReceipt(electionId: string): VoteReceipt | null {
    const receipts = this.voteReceiptsSubject.value;
    return receipts.get(electionId) || null;
  }

  /**
   * Verificar si el usuario ha votado en una elección
   * Comprueba si existe un recibo en cache
   *
   * @param electionId ID de la elección
   * @returns Boolean indicando si ha votado
   */
  hasVoted(electionId: string): boolean {
    return this.voteReceiptsSubject.value.has(electionId);
  }

  /**
   * Obtener todos los recibos de voto del usuario
   *
   * @returns Array de recibos de voto
   */
  getAllReceipts(): VoteReceipt[] {
    return Array.from(this.voteReceiptsSubject.value.values());
  }

  /**
   * Verificar recibo con blockchain
   * Valida que el hash del recibo coincide con el registro en blockchain
   *
   * @param receipt Recibo a verificar
   * @returns Observable con resultado de verificación
   */
  verifyReceipt(receipt: VoteReceipt): Observable<boolean> {
    // TODO: Implementar verificación real con blockchain cuando esté disponible el endpoint
    // Por ahora retorna una verificación simulada

    if (!environment.features.enableBlockchainVerification) {
      return throwError(() => new Error('Blockchain verification is disabled'));
    }

    // Simulación: verificar que el recibo tiene todos los campos requeridos
    const isValid = !!(
      receipt.receiptId &&
      receipt.blockchainTxId &&
      receipt.verificationHash &&
      receipt.storageReference
    );

    return new Observable(observer => {
      setTimeout(() => {
        observer.next(isValid);
        observer.complete();
      }, 1000);
    });
  }

  /**
   * Limpiar recibo de una elección específica
   * Útil si se permite modificar votos
   *
   * @param electionId ID de la elección
   */
  clearReceipt(electionId: string): void {
    const receipts = this.voteReceiptsSubject.value;
    receipts.delete(electionId);
    this.voteReceiptsSubject.next(receipts);
    this.saveReceiptsToStorage();
  }

  /**
   * Limpiar todos los recibos
   * Útil al cerrar sesión
   */
  clearAllReceipts(): void {
    this.voteReceiptsSubject.next(new Map());
    localStorage.removeItem('vote_receipts');
  }

  /**
   * Exportar recibo como texto para guardar/imprimir
   *
   * @param receipt Recibo a exportar
   * @returns String formateado con información del recibo
   */
  exportReceiptAsText(receipt: VoteReceipt): string {
    return `
═══════════════════════════════════════════════════════
  RECIBO DE VOTO - SISTEMA DE VOTACIÓN BLOCKCHAIN
═══════════════════════════════════════════════════════

Elección: ${receipt.electionTitle}
ID de Recibo: ${receipt.receiptId}
Fecha: ${new Date(receipt.timestamp).toLocaleString('es-ES')}

─────────────────────────────────────────────────────────
INFORMACIÓN DE BLOCKCHAIN
─────────────────────────────────────────────────────────

Transaction ID: ${receipt.blockchainTxId}
Hash de Verificación: ${receipt.verificationHash}
Referencia de Almacenamiento: ${receipt.storageReference}

─────────────────────────────────────────────────────────
INSTRUCCIONES
─────────────────────────────────────────────────────────

${receipt.instructions}

═══════════════════════════════════════════════════════
Guarde este recibo para verificar su voto posteriormente.
No comparta esta información con terceros.
═══════════════════════════════════════════════════════
    `.trim();
  }

  /**
   * Mapear respuesta del backend a VoteReceipt
   */
  private mapToVoteReceipt(response: any, electionId: string): VoteReceipt {
    return {
      receiptId: this.generateReceiptId(),
      electionId: electionId,
      electionTitle: response.electionTitle || 'Election',
      timestamp: response.timestamp || new Date().toISOString(),
      blockchainTxId: response.transactionId,
      verificationHash: response.commitment,
      storageReference: response.blobId,
      instructions: response.message || 'Su voto ha sido registrado correctamente.'
    };
  }

  /**
   * Almacenar recibo en cache y localStorage
   */
  private storeReceipt(electionId: string, receipt: VoteReceipt): void {
    const receipts = this.voteReceiptsSubject.value;
    receipts.set(electionId, receipt);
    this.voteReceiptsSubject.next(receipts);
    this.saveReceiptsToStorage();
  }

  /**
   * Guardar recibos en localStorage
   */
  private saveReceiptsToStorage(): void {
    const receipts = this.voteReceiptsSubject.value;
    const receiptsArray = Array.from(receipts.entries());
    localStorage.setItem('vote_receipts', JSON.stringify(receiptsArray));
  }

  /**
   * Cargar recibos desde localStorage
   */
  private loadReceiptsFromStorage(): void {
    try {
      const stored = localStorage.getItem('vote_receipts');
      if (stored) {
        const receiptsArray = JSON.parse(stored) as [string, VoteReceipt][];
        const receiptsMap = new Map(receiptsArray);
        this.voteReceiptsSubject.next(receiptsMap);
      }
    } catch (error) {
      console.error('Error loading vote receipts from storage:', error);
    }
  }

  /**
   * Generar ID único para recibo
   */
  private generateReceiptId(): string {
    return `RCPT-${Date.now()}-${Math.random().toString(36).substr(2, 9).toUpperCase()}`;
  }

  /**
   * Manejo centralizado de errores
   */
  private handleError(error: any): Observable<never> {
    let errorMessage = 'An error occurred while submitting vote';

    if (error.error instanceof ErrorEvent) {
      // Error del cliente
      errorMessage = `Client Error: ${error.error.message}`;
    } else if (error.error && typeof error.error === 'object') {
      // Error del servidor con formato ErrorResponse
      const errorResponse = error.error as ErrorResponse;
      errorMessage = errorResponse.message || errorResponse.error || 'Server Error';

      // Mensajes específicos para errores comunes de votación
      if (error.status === 409) {
        errorMessage = 'Ya ha votado en esta elección';
      } else if (error.status === 400 && errorMessage.includes('not active')) {
        errorMessage = 'La votación no está activa en este momento';
      } else if (error.status === 404) {
        errorMessage = 'La elección no fue encontrada';
      }
    } else if (error.message) {
      errorMessage = error.message;
    }

    console.error('VoteService Error:', errorMessage, error);
    return throwError(() => new Error(errorMessage));
  }
}
