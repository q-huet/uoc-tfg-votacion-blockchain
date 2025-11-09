package es.tfg.votacion.model;

import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Enumeración que define los estados de una elección
 * 
 * Estados del ciclo de vida:
 * - DRAFT: Elección creada pero no publicada
 * - ACTIVE: Elección activa y aceptando votos
 * - CLOSED: Elección cerrada, no acepta más votos
 * - COMPLETED: Elección completada con resultados finalizados
 * - CANCELLED: Elección cancelada
 * 
 * @author Enrique Huet Adrover
 * @version 1.0
 * @since Java 21
 */
public enum ElectionStatus {

    /**
     * Elección en borrador - no publicada aún
     */
    DRAFT("draft", "Borrador"),

    /**
     * Elección activa - aceptando votos
     */
    ACTIVE("active", "Activa"),

    /**
     * Elección cerrada - no acepta más votos
     */
    CLOSED("closed", "Cerrada"),

    /**
     * Elección completada - resultados finalizados
     */
    COMPLETED("completed", "Completada"),

    /**
     * Elección cancelada
     */
    CANCELLED("cancelled", "Cancelada");

    private final String code;
    private final String displayName;

    /**
     * Constructor del enum
     * 
     * @param code        Código del estado (usado en JSON/API)
     * @param displayName Nombre para mostrar en UI
     */
    ElectionStatus(String code, String displayName) {
        this.code = code;
        this.displayName = displayName;
    }

    /**
     * Obtiene el código del estado para serialización JSON
     * 
     * @return Código del estado
     */
    @JsonValue
    public String getCode() {
        return code;
    }

    /**
     * Obtiene el nombre para mostrar
     * 
     * @return Nombre del estado para UI
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Busca un estado por su código
     * 
     * @param code Código a buscar
     * @return ElectionStatus correspondiente
     * @throws IllegalArgumentException si el código no existe
     */
    public static ElectionStatus fromCode(String code) {
        if (code == null || code.isBlank()) {
            throw new IllegalArgumentException("Election status code cannot be null or blank");
        }

        for (ElectionStatus status : values()) {
            if (status.code.equalsIgnoreCase(code.trim())) {
                return status;
            }
        }

        throw new IllegalArgumentException("Unknown election status code: " + code);
    }

    /**
     * Verifica si el estado permite votar
     * 
     * @return true si se puede votar en este estado
     */
    public boolean allowsVoting() {
        return this == ACTIVE;
    }

    /**
     * Verifica si el estado es final (no se puede cambiar)
     * 
     * @return true si es un estado final
     */
    public boolean isFinal() {
        return this == COMPLETED || this == CANCELLED;
    }

    /**
     * Verifica si se pueden obtener resultados
     * 
     * @return true si tiene resultados disponibles
     */
    public boolean hasResults() {
        return this == CLOSED || this == COMPLETED;
    }

    @Override
    public String toString() {
        return displayName + " (" + code + ")";
    }
}