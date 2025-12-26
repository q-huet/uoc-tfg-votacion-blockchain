package es.tfg.votacion.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Request para cerrar una elección.
 * Requiere la clave privada para realizar el recuento (descifrado).
 */
public record CloseElectionRequest(
    @NotBlank(message = "Private key is required for recount")
    String privateKey
) {}
