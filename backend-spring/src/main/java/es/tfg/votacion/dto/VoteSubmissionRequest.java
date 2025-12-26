package es.tfg.votacion.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * DTO para request de emisión de voto
 * 
 * @author Enrique Huet Adrover
 */
public record VoteSubmissionRequest(
    @NotBlank(message = "Election ID is required")
    String electionId,
    
    String optionId, // Opcional si se usa encryptedPayload
    
    String encryptedPayload, // Voto cifrado con RSA (Client-Side Encryption)
    
    String comment  // Opcional: comentario cifrado
) {}
