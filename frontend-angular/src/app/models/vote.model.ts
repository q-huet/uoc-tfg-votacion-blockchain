/**
 * Request para emitir un voto
 * Coincide con VoteSubmissionRequest DTO del backend
 */
export interface VoteSubmissionRequest {
  electionId: string;
  optionId?: string; // Opcional si se usa encryptedPayload
  encryptedPayload?: string; // Voto cifrado con RSA
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


