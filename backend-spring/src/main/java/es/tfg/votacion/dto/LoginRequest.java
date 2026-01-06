package es.tfg.votacion.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * DTO para request de login
 * 
 * @author Enrique Huet Adrover
 */
public record LoginRequest(
    @NotBlank(message = "Username is required")
    String username,
    
    @NotBlank(message = "Password is required")
    String password
) {}
