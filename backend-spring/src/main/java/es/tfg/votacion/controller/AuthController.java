package es.tfg.votacion.controller;

import es.tfg.votacion.dto.ErrorResponse;
import es.tfg.votacion.dto.LoginRequest;
import es.tfg.votacion.dto.LoginResponse;
import es.tfg.votacion.dto.TokenValidationResponse;
import es.tfg.votacion.model.User;
import es.tfg.votacion.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Controlador para gestión de autenticación simulada (mock Azure EntraID)
 * 
 * Endpoints:
 * - POST /api/v1/auth/login - Autenticación con username/password
 * - GET /api/v1/auth/validate - Validación de JWT token
 * - GET /api/v1/auth/user - Información del usuario autenticado
 * - POST /api/v1/auth/logout - Logout (informacional, JWT es stateless)
 * 
 * @author Enrique Huet Adrover
 * @version 1.0
 * @since Java 21
 */
@RestController
@RequestMapping("/api/v1/auth")
@CrossOrigin(origins = { "http://localhost:4200", "http://127.0.0.1:4200" })
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);
    
    private final AuthService authService;
    
    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * Endpoint para login con credenciales mock
     * POST /api/v1/auth/login
     * 
     * @param loginRequest Credenciales del usuario (username, password)
     * @param request HTTP request para logging
     * @return JWT token y datos del usuario si autenticación exitosa
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(
            @Valid @RequestBody LoginRequest loginRequest,
            HttpServletRequest request) {
        
        logger.info("Login attempt for user: {} from IP: {}", 
            loginRequest.username(), 
            request.getRemoteAddr());
        
        try {
            // Autenticar usuario
            User user = authService.authenticateUser(
                loginRequest.username(), 
                loginRequest.password()
            );
            
            if (user == null) {
                logger.warn("Authentication failed for user: {}", loginRequest.username());
                return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(new ErrorResponse(
                        401,
                        "Unauthorized",
                        "Invalid username or password",
                        request.getRequestURI()
                    ));
            }
            
            // Generar JWT token
            String token = authService.generateJwtToken(user);
            
            // Crear response con información del usuario
            LoginResponse.UserInfo userInfo = new LoginResponse.UserInfo(
                user.id(),
                user.username(),
                user.email(),
                user.fullName(),
                user.role(),
                user.department(),
                user.lastLogin()
            );
            
            LoginResponse response = new LoginResponse(
                token,
                3600, // JWT expiration in seconds (1 hour)
                userInfo
            );
            
            logger.info("User authenticated successfully: {} (role: {})", 
                user.username(), user.role().getDisplayName());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Error during authentication for user: {}", loginRequest.username(), e);
            return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse(
                    500,
                    "Internal Server Error",
                    "An error occurred during authentication",
                    request.getRequestURI()
                ));
        }
    }

    /**
     * Endpoint para validar un JWT token
     * GET /api/v1/auth/validate
     * 
     * @param authHeader Authorization header con Bearer token
     * @param request HTTP request para logging
     * @return Información de validación del token
     */
    @GetMapping("/validate")
    public ResponseEntity<?> validateToken(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            HttpServletRequest request) {
        
        logger.debug("Token validation requested from IP: {}", request.getRemoteAddr());
        
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            logger.warn("Missing or invalid Authorization header");
            return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(TokenValidationResponse.invalid("Missing or invalid Authorization header"));
        }
        
        String token = authHeader.substring(7); // Remove "Bearer " prefix
        
        try {
            // Validar token y obtener usuario
            User user = authService.validateJwtToken(token);
            
            if (user == null) {
                logger.warn("Invalid or expired JWT token");
                return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(TokenValidationResponse.invalid("Invalid or expired token"));
            }
            
            logger.debug("Token validated successfully for user: {}", user.username());
            
            return ResponseEntity.ok(
                TokenValidationResponse.valid(user.username(), user.email(), user.role())
            );
            
        } catch (Exception e) {
            logger.error("Error validating token", e);
            return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(TokenValidationResponse.invalid("Token validation error: " + e.getMessage()));
        }
    }

    /**
     * Endpoint para obtener información del usuario autenticado
     * GET /api/v1/auth/user
     * 
     * @param authHeader Authorization header con Bearer token
     * @param request HTTP request para logging
     * @return Información completa del usuario
     */
    @GetMapping("/user")
    public ResponseEntity<?> getCurrentUser(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            HttpServletRequest request) {
        
        logger.debug("User info requested from IP: {}", request.getRemoteAddr());
        
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            logger.warn("Missing or invalid Authorization header");
            return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(new ErrorResponse(
                    401,
                    "Unauthorized",
                    "Missing or invalid Authorization header",
                    request.getRequestURI()
                ));
        }
        
        String token = authHeader.substring(7);
        
        try {
            User user = authService.validateJwtToken(token);
            
            if (user == null) {
                return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(new ErrorResponse(
                        401,
                        "Unauthorized",
                        "Invalid or expired token",
                        request.getRequestURI()
                    ));
            }
            
            logger.debug("User info retrieved for: {}", user.username());
            
            return ResponseEntity.ok(new LoginResponse.UserInfo(
                user.id(),
                user.username(),
                user.email(),
                user.fullName(),
                user.role(),
                user.department(),
                user.lastLogin()
            ));
            
        } catch (Exception e) {
            logger.error("Error retrieving user info", e);
            return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse(
                    500,
                    "Internal Server Error",
                    "Error retrieving user information",
                    request.getRequestURI()
                ));
        }
    }

    /**
     * Endpoint para logout
     * POST /api/v1/auth/logout
     * 
     * Nota: Como JWT es stateless, este endpoint es solo informacional.
     * El cliente debe eliminar el token localmente.
     * 
     * @param authHeader Authorization header con Bearer token
     * @param request HTTP request para logging
     * @return Mensaje de confirmación
     */
    @PostMapping("/logout")
    public ResponseEntity<?> logout(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            HttpServletRequest request) {
        
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            try {
                String username = authService.extractUsername(token);
                logger.info("User logged out: {} from IP: {}", username, request.getRemoteAddr());
            } catch (Exception e) {
                logger.debug("Could not extract username from token during logout");
            }
        }
        
        return ResponseEntity.ok(new LogoutResponse(
            "Logout successful",
            "Token should be removed from client storage"
        ));
    }
    
    /**
     * DTO interno para response de logout
     */
    private record LogoutResponse(String message, String instruction) {}
}