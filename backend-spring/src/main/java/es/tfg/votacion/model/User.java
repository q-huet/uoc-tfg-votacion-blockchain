package es.tfg.votacion.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

/**
 * Record que representa un usuario del sistema de votación (mock de Azure
 * EntraID)
 * 
 * Inmutable por diseño (Java 21 Record)
 * Incluye validaciones JSR-380 para garantizar integridad de datos
 * 
 * @param id         Identificador único del usuario
 * @param username   Nombre de usuario (empleado)
 * @param email      Correo electrónico corporativo
 * @param fullName   Nombre completo del empleado
 * @param role       Rol en el sistema (voter, admin, auditor)
 * @param department Departamento de trabajo
 * @param active     Estado activo del usuario
 * @param lastLogin  Última vez que se autenticó
 * @param createdAt  Fecha de creación del usuario
 * 
 * @author Enrique Huet Adrover
 * @version 1.0
 * @since Java 21
 */
public record User(
        @JsonProperty("id") @NotBlank(message = "User ID cannot be blank") @Size(min = 3, max = 50, message = "User ID must be between 3 and 50 characters") String id,

        @JsonProperty("username") @NotBlank(message = "Username cannot be blank") @Size(min = 3, max = 30, message = "Username must be between 3 and 30 characters") @Pattern(regexp = "^[a-zA-Z0-9._-]+$", message = "Username can only contain alphanumeric characters, dots, underscores and hyphens") String username,

        @JsonProperty("email") @NotBlank(message = "Email cannot be blank") @Pattern(regexp = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$", message = "Invalid email format") String email,

        @JsonProperty("fullName") @NotBlank(message = "Full name cannot be blank") @Size(min = 2, max = 100, message = "Full name must be between 2 and 100 characters") String fullName,

        @JsonProperty("role") @NotNull(message = "Role cannot be null") UserRole role,

        @JsonProperty("department") @Size(max = 50, message = "Department name cannot exceed 50 characters") String department,

        @JsonProperty("active") @NotNull(message = "Active status cannot be null") Boolean active,

        @JsonProperty("lastLogin") LocalDateTime lastLogin,

        @JsonProperty("createdAt") @NotNull(message = "Creation date cannot be null") LocalDateTime createdAt) {

    /**
     * Constructor compacto con validaciones adicionales
     */
    public User {
        // Validaciones adicionales que no se pueden hacer con anotaciones
        if (role == null) {
            throw new IllegalArgumentException("Role cannot be null");
        }

        if (active == null) {
            active = true; // Valor por defecto
        }

        if (createdAt == null) {
            createdAt = LocalDateTime.now(); // Valor por defecto
        }

        // Normalizar email a minúsculas
        if (email != null) {
            email = email.toLowerCase().trim();
        }

        // Normalizar username
        if (username != null) {
            username = username.toLowerCase().trim();
        }

        // Normalizar department
        if (department != null && department.isBlank()) {
            department = null;
        }
    }

    /**
     * Factory method para crear un usuario básico
     * 
     * @param username Nombre de usuario
     * @param email    Correo electrónico
     * @param fullName Nombre completo
     * @param role     Rol del usuario
     * @return Usuario creado con valores por defecto
     */
    public static User createBasicUser(String username, String email, String fullName, UserRole role) {
        return new User(
                generateUserId(username),
                username,
                email,
                fullName,
                role,
                null, // Sin department por defecto
                true, // Activo por defecto
                null, // Sin último login
                LocalDateTime.now() // Creado ahora
        );
    }

    /**
     * Crea una copia del usuario con último login actualizado
     * 
     * @return Usuario con lastLogin actualizado
     */
    public User withUpdatedLastLogin() {
        return new User(id, username, email, fullName, role, department,
                active, LocalDateTime.now(), createdAt);
    }

    /**
     * Crea una copia del usuario con estado activo modificado
     * 
     * @param newActiveState Nuevo estado activo
     * @return Usuario con estado modificado
     */
    public User withActiveStatus(boolean newActiveState) {
        return new User(id, username, email, fullName, role, department,
                newActiveState, lastLogin, createdAt);
    }

    /**
     * Verifica si el usuario tiene un rol específico
     * 
     * @param requiredRole Rol requerido
     * @return true si el usuario tiene el rol
     */
    public boolean hasRole(UserRole requiredRole) {
        return this.role == requiredRole;
    }

    /**
     * Verifica si el usuario puede votar
     * 
     * @return true si está activo y tiene rol de voter o admin
     */
    public boolean canVote() {
        return active && (role == UserRole.VOTER || role == UserRole.ADMIN);
    }

    /**
     * Verifica si el usuario puede administrar elecciones
     * 
     * @return true si está activo y tiene rol de admin
     */
    public boolean canAdministerElections() {
        return active && role == UserRole.ADMIN;
    }

    /**
     * Verifica si el usuario puede auditar el sistema
     * 
     * @return true si está activo y tiene rol de auditor
     */
    public boolean canAudit() {
        return active && role == UserRole.AUDITOR;
    }

    /**
     * Genera un ID único para el usuario basado en el username
     * 
     * @param username Nombre de usuario
     * @return ID único generado
     */
    private static String generateUserId(String username) {
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("Username cannot be null or blank for ID generation");
        }
        // En un sistema real, esto sería un UUID o ID de base de datos
        return "user_" + username.toLowerCase().trim() + "_" + System.currentTimeMillis();
    }
}