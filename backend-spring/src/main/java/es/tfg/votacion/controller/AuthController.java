package es.tfg.votacion.controller;

import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Controlador para gestión de autenticación simulada (mock Azure EntraID)
 * 
 * @author Enrique Huet Adrover
 * @version 1.0
 * @since Java 21
 */
@RestController
@RequestMapping("/auth")
@CrossOrigin(origins = { "http://localhost:4200", "http://127.0.0.1:4200" })
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    // TODO: Inyectar AuthService

    /**
     * Endpoint para login simulado
     * POST /api/v1/auth/login
     * 
     * @param loginRequest Credenciales del usuario
     * @return JWT token simulado
     */
    @PostMapping("/login")
    public Object login(@RequestBody Object loginRequest) {
        logger.info("Login attempt received");
        // TODO: Implementar lógica de autenticación mock
        return "TODO: Implement login logic";
    }

    /**
     * Endpoint para validar token JWT
     * GET /api/v1/auth/validate
     * 
     * @param token JWT token a validar
     * @return Información del usuario autenticado
     */
    @GetMapping("/validate")
    public Object validateToken(@RequestHeader("Authorization") String token) {
        logger.info("Token validation requested");
        // TODO: Implementar validación de token
        return "TODO: Implement token validation";
    }

    /**
     * Endpoint para logout (invalidar token)
     * POST /api/v1/auth/logout
     */
    @PostMapping("/logout")
    public Object logout() {
        logger.info("Logout requested");
        // TODO: Implementar logout
        return "TODO: Implement logout";
    }
}