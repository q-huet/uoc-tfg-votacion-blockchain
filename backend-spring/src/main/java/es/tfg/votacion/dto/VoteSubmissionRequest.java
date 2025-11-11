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
    
    @NotBlank(message = "Option ID is required")
    String optionId,
    
    String comment  // Opcional: comentario cifrado
) {}
