import { ElectionStatus } from './election-status.enum';
import { ElectionOption } from './election-option.model';

/**
 * Modelo completo de Elección
 * Coincide con Election.java (record) del backend
 */
export interface Election {
  id: string;
  title: string;
  description: string;
  options: ElectionOption[];
  status: ElectionStatus;
  startTime: string; // ISO 8601 datetime string
  endTime: string; // ISO 8601 datetime string
  createdBy: string;
  createdAt: string; // ISO 8601 datetime string
  totalVotes: number;
  maxVotesPerUser: number;
  allowVoteModification: boolean;
  requireAuditTrail: boolean;
  hasVoted?: boolean;
  publicKey?: string; // Clave pública RSA para cifrado de votos
}

/**
 * Resultado de la creación de una elección
 */
export interface ElectionCreationResult {
  election: Election;
  privateKey: string; // Clave privada RSA (solo se devuelve una vez)
}

/**
 * Resumen de elección para listados
 * Coincide con ElectionSummary DTO del backend
 */
export interface ElectionSummary {
  electionId: string;
  title: string;
  description: string;
  status: ElectionStatus;
  startTime: string;
  endTime: string;
  totalVotes: number;
  hasVoted: boolean;
  canVote: boolean;
}

/**
 * Request para crear una nueva elección
 */
export interface CreateElectionRequest {
  title: string;
  description: string;
  options: ElectionOption[];
  startTime: string;
  endTime: string;
}

export interface ElectionResultOption {
  optionId: string;
  label: string;
  votes: number;
  percentage: number;
}

export interface ElectionResults {
  electionId: string;
  title: string;
  status: ElectionStatus;
  closedAt: string;
  totalVotes: number;
  results: ElectionResultOption[];
  auditTrail: string;
}
