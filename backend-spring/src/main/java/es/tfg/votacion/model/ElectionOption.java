package es.tfg.votacion.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Opción de voto en una elección
 * 
 * Representa cada una de las opciones disponibles para votar en una elección
 * específica.
 * Utiliza Java 21 Records para inmutabilidad y validaciones robustas.
 * 
 * @param optionId     Identificador único de la opción
 * @param title        Título de la opción (ej: nombre del candidato)
 * @param description  Descripción opcional de la opción
 * @param displayOrder Orden de visualización en la UI
 * 
 * @author Enrique Huet Adrover
 * @version 1.0
 * @since Java 21
 */
public record ElectionOption(

        @NotBlank(message = "Option ID is required") @Size(min = 1, max = 50, message = "Option ID must be between 1 and 50 characters") @Pattern(regexp = "^[a-zA-Z0-9_-]+$", message = "Option ID must contain only alphanumeric characters, hyphens, and underscores") @JsonProperty("optionId") String optionId,

        @NotBlank(message = "Option title is required") @Size(min = 1, max = 200, message = "Option title must be between 1 and 200 characters") @JsonProperty("title") String title,

        @Size(max = 1000, message = "Option description cannot exceed 1000 characters") @JsonProperty("description") String description,

        @NotNull(message = "Display order is required") @JsonProperty("displayOrder") Integer displayOrder

) {

    /**
     * Constructor con validación
     */
    public ElectionOption {
        // Normalización y validación
        if (optionId != null) {
            optionId = optionId.trim().toLowerCase();
        }

        if (title != null) {
            title = title.trim();
        }

        if (description != null) {
            description = description.trim();
            // Si está vacío después del trim, lo convertimos a null
            if (description.isEmpty()) {
                description = null;
            }
        }

        // Validación de display order
        if (displayOrder != null && displayOrder < 0) {
            throw new IllegalArgumentException("Display order cannot be negative");
        }

        // Validación de optionId format
        if (optionId != null && !optionId.matches("^[a-zA-Z0-9_-]+$")) {
            throw new IllegalArgumentException("Option ID contains invalid characters");
        }
    }

    /**
     * Constructor para deserialización JSON
     * 
     * @param optionId     Identificador de la opción
     * @param title        Título de la opción
     * @param description  Descripción de la opción
     * @param displayOrder Orden de visualización
     */
    @JsonCreator
    public static ElectionOption create(
            @JsonProperty("optionId") String optionId,
            @JsonProperty("title") String title,
            @JsonProperty("description") String description,
            @JsonProperty("displayOrder") Integer displayOrder) {
        return new ElectionOption(optionId, title, description, displayOrder);
    }

    /**
     * Factory method para crear una opción simple
     * 
     * @param optionId     ID de la opción
     * @param title        Título de la opción
     * @param displayOrder Orden de visualización
     * @return Nueva instancia de ElectionOption
     */
    public static ElectionOption of(String optionId, String title, Integer displayOrder) {
        return new ElectionOption(optionId, title, null, displayOrder);
    }

    /**
     * Factory method para crear una opción con descripción
     * 
     * @param optionId     ID de la opción
     * @param title        Título de la opción
     * @param description  Descripción de la opción
     * @param displayOrder Orden de visualización
     * @return Nueva instancia de ElectionOption
     */
    public static ElectionOption withDescription(String optionId, String title,
            String description, Integer displayOrder) {
        return new ElectionOption(optionId, title, description, displayOrder);
    }

    /**
     * Verifica si la opción tiene descripción
     * 
     * @return true si tiene descripción no vacía
     */
    public boolean hasDescription() {
        return description != null && !description.isEmpty();
    }

    /**
     * Crea una copia con nuevo orden de visualización
     * 
     * @param newDisplayOrder Nuevo orden
     * @return Nueva instancia con el orden actualizado
     */
    public ElectionOption withDisplayOrder(Integer newDisplayOrder) {
        return new ElectionOption(optionId, title, description, newDisplayOrder);
    }

    /**
     * Crea una copia con nueva descripción
     * 
     * @param newDescription Nueva descripción
     * @return Nueva instancia con la descripción actualizada
     */
    public ElectionOption withDescription(String newDescription) {
        return new ElectionOption(optionId, title, newDescription, displayOrder);
    }

    /**
     * Crea una copia con nuevo título
     * 
     * @param newTitle Nuevo título
     * @return Nueva instancia con el título actualizado
     */
    public ElectionOption withTitle(String newTitle) {
        return new ElectionOption(optionId, newTitle, description, displayOrder);
    }

    /**
     * Representación para logging y debugging
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("ElectionOption{")
                .append("id='").append(optionId).append("'")
                .append(", title='").append(title).append("'")
                .append(", order=").append(displayOrder);

        if (hasDescription()) {
            sb.append(", hasDescription=true");
        }

        sb.append("}");
        return sb.toString();
    }
}