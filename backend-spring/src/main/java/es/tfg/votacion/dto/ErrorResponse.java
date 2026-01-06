package es.tfg.votacion.dto;

import java.time.Instant;

/**
 * DTO para respuestas de error estandarizadas
 * 
 * @author Enrique Huet Adrover
 */
public record ErrorResponse(
    int status,
    String error,
    String message,
    String path,
    Instant timestamp
) {
    public ErrorResponse(int status, String error, String message, String path) {
        this(status, error, message, path, Instant.now());
    }
}
