package es.tfg.votacion.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Record que representa una elección sindical
 * 
 * Inmutable por diseño (Java 21 Record)
 * Incluye validaciones JSR-380 y métodos de utilidad
 * 
 * @param id                    Identificador único de la elección
 * @param title                 Título de la elección
 * @param description           Descripción detallada
 * @param options               Lista de opciones disponibles para votar
 * @param status                Estado actual de la elección
 * @param startTime             Fecha y hora de inicio
 * @param endTime               Fecha y hora de fin
 * @param createdBy             Usuario que creó la elección
 * @param createdAt             Fecha de creación
 * @param totalVotes            Número total de votos emitidos
 * @param maxVotesPerUser       Máximo de votos por usuario
 * @param allowVoteModification Si se permite modificar votos
 * @param requireAuditTrail     Si requiere auditoría
 * 
 * @author Enrique Huet Adrover
 * @version 1.0
 * @since Java 21
 */
public record Election(
        @JsonProperty("id") @NotBlank(message = "Election ID cannot be blank") String id,

        @JsonProperty("title") @NotBlank(message = "Election title cannot be blank") @Size(min = 5, max = 100, message = "Title must be between 5 and 100 characters") String title,

        @JsonProperty("description") @NotBlank(message = "Election description cannot be blank") @Size(min = 10, max = 500, message = "Description must be between 10 and 500 characters") String description,

        @JsonProperty("options") @NotNull(message = "Election options cannot be null") @Size(min = 2, max = 10, message = "Election must have between 2 and 10 options") @Valid List<ElectionOption> options,

        @JsonProperty("status") @NotNull(message = "Election status cannot be null") ElectionStatus status,

        @JsonProperty("startTime") @NotNull(message = "Start time cannot be null") @Future(message = "Start time must be in the future") LocalDateTime startTime,

        @JsonProperty("endTime") @NotNull(message = "End time cannot be null") @Future(message = "End time must be in the future") LocalDateTime endTime,

        @JsonProperty("createdBy") @NotBlank(message = "Created by cannot be blank") String createdBy,

        @JsonProperty("createdAt") @NotNull(message = "Creation date cannot be null") LocalDateTime createdAt,

        @JsonProperty("totalVotes") @Min(value = 0, message = "Total votes cannot be negative") int totalVotes,

        @JsonProperty("maxVotesPerUser") @Min(value = 1, message = "Max votes per user must be at least 1") @Max(value = 10, message = "Max votes per user cannot exceed 10") int maxVotesPerUser,

        @JsonProperty("allowVoteModification") @NotNull(message = "Allow vote modification cannot be null") Boolean allowVoteModification,

        @JsonProperty("requireAuditTrail") @NotNull(message = "Require audit trail cannot be null") Boolean requireAuditTrail) {

    /**
     * Constructor compacto con validaciones adicionales
     */
    public Election {
        // Validar que endTime sea posterior a startTime
        if (startTime != null && endTime != null && !endTime.isAfter(startTime)) {
            throw new IllegalArgumentException("End time must be after start time");
        }

        // Validar que las opciones no estén vacías
        if (options != null) {
            for (ElectionOption option : options) {
                if (option == null || option.title() == null || option.title().isBlank()) {
                    throw new IllegalArgumentException("All election options must have valid text");
                }
            }
        }

        // Valores por defecto
        if (status == null) {
            status = ElectionStatus.DRAFT;
        }

        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }

        if (allowVoteModification == null) {
            allowVoteModification = false;
        }

        if (requireAuditTrail == null) {
            requireAuditTrail = true;
        }
    }

    /**
     * Factory method para crear una elección básica
     * 
     * @param title         Título de la elección
     * @param description   Descripción
     * @param options       Lista de opciones
     * @param durationHours Duración en horas
     * @param createdBy     Usuario creador
     * @return Elección creada
     */
    public static Election createBasic(String title, String description, List<ElectionOption> options,
            int durationHours, String createdBy) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime start = now.plusMinutes(5); // Inicia en 5 minutos
        LocalDateTime end = start.plusHours(durationHours);

        return new Election(
                generateElectionId(title),
                title,
                description,
                options,
                ElectionStatus.DRAFT,
                start,
                end,
                createdBy,
                now,
                0, // Sin votos inicialmente
                1, // Un voto por usuario por defecto
                false, // No modificación por defecto
                true // Auditoría requerida por defecto
        );
    }

    /**
     * Verifica si la elección está activa para votar
     * 
     * @return true si está en estado ACTIVE y dentro del período
     */
    public boolean isActiveForVoting() {
        LocalDateTime now = LocalDateTime.now();
        return status == ElectionStatus.ACTIVE &&
                now.isAfter(startTime) &&
                now.isBefore(endTime);
    }

    /**
     * Verifica si la elección ha finalizado
     * 
     * @return true si está cerrada o ha pasado el tiempo
     */
    public boolean hasEnded() {
        LocalDateTime now = LocalDateTime.now();
        return status == ElectionStatus.CLOSED ||
                status == ElectionStatus.COMPLETED ||
                now.isAfter(endTime);
    }

    /**
     * Crea una copia con el estado actualizado
     * 
     * @param newStatus Nuevo estado
     * @return Elección con estado actualizado
     */
    public Election withStatus(ElectionStatus newStatus) {
        return new Election(id, title, description, options, newStatus, startTime, endTime,
                createdBy, createdAt, totalVotes, maxVotesPerUser,
                allowVoteModification, requireAuditTrail);
    }

    /**
     * Crea una copia con el contador de votos actualizado
     * 
     * @param newTotalVotes Nuevo total de votos
     * @return Elección con votos actualizados
     */
    public Election withTotalVotes(int newTotalVotes) {
        if (newTotalVotes < 0) {
            throw new IllegalArgumentException("Total votes cannot be negative");
        }

        return new Election(id, title, description, options, status, startTime, endTime,
                createdBy, createdAt, newTotalVotes, maxVotesPerUser,
                allowVoteModification, requireAuditTrail);
    }

    /**
     * Obtiene una opción por su ID
     * 
     * @param optionId ID de la opción
     * @return Opción encontrada o null
     */
    public ElectionOption getOptionById(String optionId) {
        if (optionId == null || options == null) {
            return null;
        }

        return options.stream()
                .filter(option -> optionId.equals(option.optionId()))
                .findFirst()
                .orElse(null);
    }

    /**
     * Calcula la duración de la elección en horas
     * 
     * @return Duración en horas
     */
    public long getDurationHours() {
        return java.time.Duration.between(startTime, endTime).toHours();
    }

    /**
     * Verifica si la elección permite votos múltiples por usuario
     * 
     * @return true si permite más de un voto por usuario
     */
    public boolean allowsMultipleVotes() {
        return maxVotesPerUser > 1;
    }

    /**
     * Genera un ID único para la elección
     * 
     * @param title Título de la elección
     * @return ID único generado
     */
    private static String generateElectionId(String title) {
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("Title cannot be null or blank for ID generation");
        }

        // Normalizar título para ID
        String normalizedTitle = title.toLowerCase()
                .replaceAll("[^a-z0-9\\s]", "")
                .replaceAll("\\s+", "-")
                .substring(0, Math.min(title.length(), 20));

        return "election_" + normalizedTitle + "_" + System.currentTimeMillis();
    }
}