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
  options: Array<{
    optionId: string;
    title: string;
    description?: string;
    displayOrder: number;
  }>;
  startTime: string;
  endTime: string;
  maxVotesPerUser: number;
  allowVoteModification: boolean;
  requireAuditTrail: boolean;
}
