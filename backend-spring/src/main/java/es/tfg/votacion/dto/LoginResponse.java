package es.tfg.votacion.dto;

import es.tfg.votacion.model.UserRole;
import java.time.LocalDateTime;

/**
 * DTO para response de login exitoso
 * 
 * @author Enrique Huet Adrover
 */
public record LoginResponse(
    String token,
    String tokenType,
    long expiresIn,
    UserInfo user
) {
    public LoginResponse(String token, long expiresIn, UserInfo user) {
        this(token, "Bearer", expiresIn, user);
    }
    
    public record UserInfo(
        String id,
        String username,
        String email,
        String fullName,
        UserRole role,
        String department,
        LocalDateTime lastLogin
    ) {}
}
