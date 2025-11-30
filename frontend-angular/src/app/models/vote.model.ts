/**
 * Request para emitir un voto
 * Coincide con VoteSubmissionRequest DTO del backend
 */
export interface VoteSubmissionRequest {
  electionId: string;
  optionId: string;
  comment?: string;
}

/**
 * Response tras emitir un voto
 * Coincide con VoteSubmissionResponse DTO del backend
 */
export interface VoteSubmissionResponse {
  success: boolean;
  message: string;
  receipt: VoteReceipt;
}

/**
 * Recibo de voto
 * Coincide con VoteReceipt DTO del backend
 */
export interface VoteReceipt {
  receiptId: string;
  electionId: string;
  electionTitle: string;
  timestamp: string; // ISO 8601 datetime string
  blockchainTxId: string;
  verificationHash: string;
  storageReference: string;
  instructions: string;
}

/**
 * Resultados de una elección (solo ADMIN/AUDITOR)
 */
export interface ElectionResults {
  electionId: string;
  title: string;
  status: string;
  totalVotes: number;
  totalEligibleVoters: number;
  participationRate: number;
  options: Array<{
    optionId: string;
    title: string;
    voteCount: number;
    percentage: number;
  }>;
  startTime: string;
  endTime: string;
  closedAt: string | null;
}
