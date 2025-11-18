import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable, throwError, BehaviorSubject } from 'rxjs';
import { map, catchError, tap } from 'rxjs/operators';
import { environment } from '@environments/environment';
import {
  Election,
  ElectionSummary,
  CreateElectionRequest
} from '@models/election.model';
import { ErrorResponse } from '@models/error.model';

/**
 * Servicio para gestión de elecciones
 *
 * Funcionalidades:
 * - Listar elecciones activas
 * - Obtener detalle de elección
 * - Crear nueva elección (ADMIN)
 * - Cerrar elección (ADMIN)
 * - Obtener resultados (ADMIN/AUDITOR)
 */
@Injectable({
  providedIn: 'root'
})
export class ElectionService {
  private readonly baseUrl = `${environment.apiUrl}${environment.endpoints.elections.base}`;

  // Estado de elecciones activas
  private activeElectionsSubject = new BehaviorSubject<ElectionSummary[]>([]);
  public activeElections$ = this.activeElectionsSubject.asObservable();

  // Estado de carga
  private loadingSubject = new BehaviorSubject<boolean>(false);
  public loading$ = this.loadingSubject.asObservable();

  constructor(private http: HttpClient) {}

  /**
   * Obtener todas las elecciones activas
   * GET /api/v1/elections
   *
   * @returns Observable con lista de elecciones
   */
  getActiveElections(): Observable<ElectionSummary[]> {
    this.loadingSubject.next(true);

    return this.http.get<any[]>(this.baseUrl).pipe(
      map(response => this.mapToElectionSummaries(response)),
      tap(elections => {
        this.activeElectionsSubject.next(elections);
        this.loadingSubject.next(false);
      }),
      catchError(error => {
        this.loadingSubject.next(false);
        return this.handleError(error);
      })
    );
  }

  /**
   * Obtener detalle de una elección específica
   * GET /api/v1/elections/{id}
   *
   * @param electionId ID de la elección
   * @returns Observable con detalle de la elección
   */
  getElectionById(electionId: string): Observable<Election> {
    this.loadingSubject.next(true);

    const url = `${this.baseUrl}/${electionId}`;

    return this.http.get<any>(url).pipe(
      map(response => this.mapToElection(response)),
      tap(() => this.loadingSubject.next(false)),
      catchError(error => {
        this.loadingSubject.next(false);
        return this.handleError(error);
      })
    );
  }

  /**
   * Crear una nueva elección (solo ADMIN)
   * POST /api/v1/elections
   *
   * @param election Datos de la nueva elección
   * @returns Observable con la elección creada
   */
  createElection(election: CreateElectionRequest): Observable<Election> {
    this.loadingSubject.next(true);

    return this.http.post<any>(this.baseUrl, election).pipe(
      map(response => this.mapToElection(response)),
      tap(() => {
        this.loadingSubject.next(false);
        // Refrescar lista de elecciones activas
        this.refreshActiveElections();
      }),
      catchError(error => {
        this.loadingSubject.next(false);
        return this.handleError(error);
      })
    );
  }

  /**
   * Cerrar una elección (solo ADMIN)
   * POST /api/v1/elections/{id}/close
   *
   * @param electionId ID de la elección a cerrar
   * @returns Observable con confirmación del cierre
   */
  closeElection(electionId: string): Observable<any> {
    this.loadingSubject.next(true);

    const url = `${this.baseUrl}/${electionId}/close`;

    return this.http.post<any>(url, {}).pipe(
      tap(() => {
        this.loadingSubject.next(false);
        // Refrescar lista de elecciones activas
        this.refreshActiveElections();
      }),
      catchError(error => {
        this.loadingSubject.next(false);
        return this.handleError(error);
      })
    );
  }

  /**
   * Obtener resultados de una elección (ADMIN/AUDITOR)
   * GET /api/v1/elections/{id}/results
   *
   * @param electionId ID de la elección
   * @returns Observable con los resultados
   */
  getElectionResults(electionId: string): Observable<any> {
    this.loadingSubject.next(true);

    const url = `${this.baseUrl}/${electionId}/results`;

    return this.http.get<any>(url).pipe(
      tap(() => this.loadingSubject.next(false)),
      catchError(error => {
        this.loadingSubject.next(false);
        return this.handleError(error);
      })
    );
  }

  /**
   * Verificar si el usuario actual ha votado en una elección
   * Esta información viene en el response de getActiveElections o getElectionById
   *
   * @param electionId ID de la elección
   * @returns Boolean indicando si ha votado
   */
  hasVotedInElection(electionId: string): boolean {
    const elections = this.activeElectionsSubject.value;
    const election = elections.find(e => e.electionId === electionId);
    return election ? election.hasVoted : false;
  }

  /**
   * Verificar si una elección está activa para votación
   *
   * @param election Elección a verificar
   * @returns Boolean indicando si está activa
   */
  isActiveForVoting(election: Election | ElectionSummary): boolean {
    const now = new Date();
    const startTime = new Date(election.startTime);
    const endTime = new Date(election.endTime);

    return election.status === 'ACTIVE' &&
           now >= startTime &&
           now <= endTime;
  }

  /**
   * Obtener elecciones filtradas por estado
   *
   * @param status Estado a filtrar
   * @returns Observable con elecciones filtradas
   */
  getElectionsByStatus(status: string): Observable<ElectionSummary[]> {
    return this.activeElections$.pipe(
      map(elections => elections.filter(e => e.status === status))
    );
  }

  /**
   * Refrescar lista de elecciones activas
   * Útil después de crear o cerrar una elección
   */
  private refreshActiveElections(): void {
    this.getActiveElections().subscribe();
  }

  /**
   * Mapear respuesta del backend a ElectionSummary
   */
  private mapToElectionSummaries(response: any[]): ElectionSummary[] {
    return response.map(item => ({
      electionId: item.electionId || item.id,
      title: item.title,
      description: item.description,
      status: item.status,
      startTime: item.startTime,
      endTime: item.endTime,
      totalVotes: item.totalVotes || 0,
      hasVoted: item.hasVoted || false,
      canVote: item.canVote !== undefined ? item.canVote : this.isActiveForVoting(item)
    }));
  }

  /**
   * Mapear respuesta del backend a Election
   */
  private mapToElection(response: any): Election {
    return {
      id: response.electionId || response.id,
      title: response.title,
      description: response.description,
      options: response.options || [],
      status: response.status,
      startTime: response.startTime,
      endTime: response.endTime,
      createdBy: response.createdBy,
      createdAt: response.createdAt,
      totalVotes: response.totalVotes || 0,
      maxVotesPerUser: response.maxVotesPerUser || 1,
      allowVoteModification: response.allowVoteModification || false,
      requireAuditTrail: response.requireAuditTrail || true
    };
  }

  /**
   * Manejo centralizado de errores
   */
  private handleError(error: any): Observable<never> {
    let errorMessage = 'An error occurred';

    if (error.error instanceof ErrorEvent) {
      // Error del cliente
      errorMessage = `Client Error: ${error.error.message}`;
    } else if (error.error && typeof error.error === 'object') {
      // Error del servidor con formato ErrorResponse
      const errorResponse = error.error as ErrorResponse;
      errorMessage = errorResponse.message || errorResponse.error || 'Server Error';
    } else if (error.message) {
      errorMessage = error.message;
    }

    console.error('ElectionService Error:', errorMessage, error);
    return throwError(() => new Error(errorMessage));
  }
}
