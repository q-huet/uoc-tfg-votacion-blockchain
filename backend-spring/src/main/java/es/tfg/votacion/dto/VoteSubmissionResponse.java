package es.tfg.votacion.dto;

import java.time.Instant;

/**
 * DTO para response de voto emitido con éxito
 * 
 * @author Enrique Huet Adrover
 */
public record VoteSubmissionResponse(
    String transactionId,
    String electionId,
    Instant timestamp,
    String commitment,
    String blobId,
    boolean verified,
    String message
) {}
