package es.tfg.votacion.dto;

import es.tfg.votacion.model.UserRole;

/**
 * DTO para response de validación de token
 * 
 * @author Enrique Huet Adrover
 */
public record TokenValidationResponse(
    boolean valid,
    String username,
    String email,
    UserRole role,
    String message
) {
    public static TokenValidationResponse valid(String username, String email, UserRole role) {
        return new TokenValidationResponse(true, username, email, role, "Token is valid");
    }
    
    public static TokenValidationResponse invalid(String message) {
        return new TokenValidationResponse(false, null, null, null, message);
    }
}
