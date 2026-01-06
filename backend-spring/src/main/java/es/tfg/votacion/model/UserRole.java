package es.tfg.votacion.model;

import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Enumeración que define los roles de usuario en el sistema de votación
 * 
 * Roles disponibles:
 * - VOTER: Empleado que puede emitir votos
 * - ADMIN: Administrador que puede gestionar elecciones
 * - AUDITOR: Auditor externo que puede verificar procesos
 * 
 * @author Enrique Huet Adrover
 * @version 1.0
 * @since Java 21
 */
public enum UserRole {

    /**
     * Empleado con derecho a voto
     * - Puede votar en elecciones activas
     * - Acceso solo a endpoints de votación
     */
    VOTER("voter", "Votante"),

    /**
     * Administrador del sistema
     * - Puede crear y gestionar elecciones
     * - Puede votar como cualquier empleado
     * - Acceso a endpoints administrativos
     */
    ADMIN("admin", "Administrador"),

    /**
     * Auditor externo del proceso
     * - Puede verificar integridad del sistema
     * - Acceso de solo lectura a resultados
     * - No puede votar
     */
    AUDITOR("auditor", "Auditor");

    private final String code;
    private final String displayName;

    /**
     * Constructor del enum
     * 
     * @param code        Código del rol (usado en JSON/API)
     * @param displayName Nombre para mostrar en UI
     */
    UserRole(String code, String displayName) {
        this.code = code;
        this.displayName = displayName;
    }

    /**
     * Obtiene el código del rol para serialización JSON
     * 
     * @return Código del rol
     */
    @JsonValue
    public String getCode() {
        return code;
    }

    /**
     * Obtiene el nombre para mostrar
     * 
     * @return Nombre del rol para UI
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Busca un rol por su código
     * 
     * @param code Código a buscar
     * @return UserRole correspondiente
     * @throws IllegalArgumentException si el código no existe
     */
    public static UserRole fromCode(String code) {
        if (code == null || code.isBlank()) {
            throw new IllegalArgumentException("Role code cannot be null or blank");
        }

        for (UserRole role : values()) {
            if (role.code.equalsIgnoreCase(code.trim())) {
                return role;
            }
        }

        throw new IllegalArgumentException("Unknown role code: " + code);
    }

    /**
     * Verifica si el rol puede votar
     * 
     * @return true si puede votar
     */
    public boolean canVote() {
        return this == VOTER || this == ADMIN;
    }

    /**
     * Verifica si el rol puede administrar
     * 
     * @return true si puede administrar
     */
    public boolean canAdminister() {
        return this == ADMIN;
    }

    /**
     * Verifica si el rol puede auditar
     * 
     * @return true si puede auditar
     */
    public boolean canAudit() {
        return this == AUDITOR;
    }

    @Override
    public String toString() {
        return displayName + " (" + code + ")";
    }
}