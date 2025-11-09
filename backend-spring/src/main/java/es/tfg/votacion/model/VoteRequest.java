package es.tfg.votacion.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

/**
 * Request de voto para una elección
 * 
 * Representa la solicitud de voto que envía un usuario para participar en una
 * elección.
 * Contiene la información necesaria para validar y procesar el voto en el
 * blockchain.
 * 
 * @param electionId       ID de la elección
 * @param userId           ID del usuario que vota
 * @param selectedOptionId ID de la opción seleccionada
 * @param timestamp        Momento en que se realizó el voto
 * @param clientIp         IP del cliente (para auditoria)
 * @param userAgent        User agent del navegador (para auditoria)
 * 
 * @author Enrique Huet Adrover
 * @version 1.0
 * @since Java 21
 */
public record VoteRequest(

        @NotBlank(message = "Election ID is required") @Size(min = 1, max = 50, message = "Election ID must be between 1 and 50 characters") @Pattern(regexp = "^[a-zA-Z0-9_-]+$", message = "Election ID must contain only alphanumeric characters, hyphens, and underscores") @JsonProperty("electionId") String electionId,

        @NotBlank(message = "User ID is required") @Size(min = 1, max = 50, message = "User ID must be between 1 and 50 characters") @JsonProperty("userId") String userId,

        @NotBlank(message = "Selected option ID is required") @Size(min = 1, max = 50, message = "Selected option ID must be between 1 and 50 characters") @Pattern(regexp = "^[a-zA-Z0-9_-]+$", message = "Selected option ID must contain only alphanumeric characters, hyphens, and underscores") @JsonProperty("selectedOptionId") String selectedOptionId,

        @NotNull(message = "Vote timestamp is required") @JsonProperty("timestamp") LocalDateTime timestamp,

        @Size(max = 45, message = "Client IP cannot exceed 45 characters") @JsonProperty("clientIp") String clientIp,

        @Size(max = 500, message = "User agent cannot exceed 500 characters") @JsonProperty("userAgent") String userAgent

) {

    /**
     * Constructor con validación
     */
    public VoteRequest {
        // Normalización
        if (electionId != null) {
            electionId = electionId.trim().toLowerCase();
        }

        if (userId != null) {
            userId = userId.trim().toLowerCase();
        }

        if (selectedOptionId != null) {
            selectedOptionId = selectedOptionId.trim().toLowerCase();
        }

        if (clientIp != null) {
            clientIp = clientIp.trim();
            // Si está vacío después del trim, lo convertimos a null
            if (clientIp.isEmpty()) {
                clientIp = null;
            }
        }

        if (userAgent != null) {
            userAgent = userAgent.trim();
            // Si está vacío después del trim, lo convertimos a null
            if (userAgent.isEmpty()) {
                userAgent = null;
            }
        }

        // Validaciones adicionales
        if (electionId != null && !electionId.matches("^[a-zA-Z0-9_-]+$")) {
            throw new IllegalArgumentException("Election ID contains invalid characters");
        }

        if (selectedOptionId != null && !selectedOptionId.matches("^[a-zA-Z0-9_-]+$")) {
            throw new IllegalArgumentException("Selected option ID contains invalid characters");
        }

        // Validar que el timestamp no sea futuro (con margen de 5 minutos)
        if (timestamp != null && timestamp.isAfter(LocalDateTime.now().plusMinutes(5))) {
            throw new IllegalArgumentException("Vote timestamp cannot be in the future");
        }

        // Validar formato básico de IP (IPv4 o IPv6)
        if (clientIp != null && !isValidIpAddress(clientIp)) {
            throw new IllegalArgumentException("Invalid client IP address format");
        }
    }

    /**
     * Constructor para deserialización JSON
     */
    @JsonCreator
    public static VoteRequest create(
            @JsonProperty("electionId") String electionId,
            @JsonProperty("userId") String userId,
            @JsonProperty("selectedOptionId") String selectedOptionId,
            @JsonProperty("timestamp") LocalDateTime timestamp,
            @JsonProperty("clientIp") String clientIp,
            @JsonProperty("userAgent") String userAgent) {
        return new VoteRequest(electionId, userId, selectedOptionId, timestamp, clientIp, userAgent);
    }

    /**
     * Factory method para crear request básico de voto
     * 
     * @param electionId       ID de la elección
     * @param userId           ID del usuario
     * @param selectedOptionId Opción seleccionada
     * @return Nueva instancia de VoteRequest con timestamp actual
     */
    public static VoteRequest of(String electionId, String userId, String selectedOptionId) {
        return new VoteRequest(electionId, userId, selectedOptionId, LocalDateTime.now(), null, null);
    }

    /**
     * Factory method para crear request con información de auditoria
     * 
     * @param electionId       ID de la elección
     * @param userId           ID del usuario
     * @param selectedOptionId Opción seleccionada
     * @param clientIp         IP del cliente
     * @param userAgent        User agent del cliente
     * @return Nueva instancia de VoteRequest
     */
    public static VoteRequest withAuditInfo(String electionId, String userId, String selectedOptionId,
            String clientIp, String userAgent) {
        return new VoteRequest(electionId, userId, selectedOptionId, LocalDateTime.now(), clientIp, userAgent);
    }

    /**
     * Verifica si la request tiene información de auditoria
     * 
     * @return true si tiene clientIp o userAgent
     */
    public boolean hasAuditInfo() {
        return (clientIp != null && !clientIp.isBlank()) ||
                (userAgent != null && !userAgent.isBlank());
    }

    /**
     * Crea una copia con nueva IP de cliente
     * 
     * @param newClientIp Nueva IP del cliente
     * @return Nueva instancia con la IP actualizada
     */
    public VoteRequest withClientIp(String newClientIp) {
        return new VoteRequest(electionId, userId, selectedOptionId, timestamp, newClientIp, userAgent);
    }

    /**
     * Crea una copia con nuevo user agent
     * 
     * @param newUserAgent Nuevo user agent
     * @return Nueva instancia con el user agent actualizado
     */
    public VoteRequest withUserAgent(String newUserAgent) {
        return new VoteRequest(electionId, userId, selectedOptionId, timestamp, clientIp, newUserAgent);
    }

    /**
     * Crea una copia con nuevo timestamp
     * 
     * @param newTimestamp Nuevo timestamp
     * @return Nueva instancia con el timestamp actualizado
     */
    public VoteRequest withTimestamp(LocalDateTime newTimestamp) {
        return new VoteRequest(electionId, userId, selectedOptionId, newTimestamp, clientIp, userAgent);
    }

    /**
     * Obtiene un identificador único para este voto (para deduplicación)
     * 
     * @return String único basado en electionId y userId
     */
    public String getVoteKey() {
        return electionId + ":" + userId;
    }

    /**
     * Verifica si la IP es válida (validación básica)
     * 
     * @param ip IP a validar
     * @return true si el formato es válido
     */
    private static boolean isValidIpAddress(String ip) {
        if (ip == null || ip.isBlank()) {
            return false;
        }

        // IPv4 básico (x.x.x.x)
        if (ip.matches("^((25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$")) {
            return true;
        }

        // IPv6 básico (contiene al menos dos ':')
        if (ip.contains(":") && ip.split(":").length >= 2) {
            return true;
        }

        return false;
    }

    /**
     * Representación para logging (sin información sensible)
     */
    @Override
    public String toString() {
        return String.format("VoteRequest{electionId='%s', userId='%s', option='%s', timestamp=%s, hasAudit=%s}",
                electionId, userId, selectedOptionId, timestamp, hasAuditInfo());
    }
}