package es.tfg.votacion.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import es.tfg.votacion.config.AuthProperties;
import es.tfg.votacion.model.User;
import es.tfg.votacion.model.UserRole;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

/**
 * Servicio para autenticación mock (simula Azure EntraID)
 * 
 * Funcionalidades:
 * - Validar usuarios desde JSON mock
 * - Generar JWT tokens simulados
 * - Validar JWT tokens
 * - Gestión de roles (voter, admin, auditor)
 * 
 * Seguridad:
 * - Contraseñas hasheadas con BCrypt
 * - JWT firmados con HS256
 * - Validación de expiración de tokens
 * 
 * @author Enrique Huet Adrover
 * @version 1.0
 * @since Java 21
 */
@Service
public class AuthService {

    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);

    private final AuthProperties authProperties;
    private final ObjectMapper objectMapper;
    private final BCryptPasswordEncoder passwordEncoder;
    
    private Map<String, MockUserData> mockUsers;
    private SecretKey jwtSigningKey;

    @Autowired
    public AuthService(AuthProperties authProperties) {
        this.authProperties = authProperties;
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
        this.passwordEncoder = new BCryptPasswordEncoder();
        this.mockUsers = new HashMap<>();
    }

    /**
     * Clase interna para almacenar datos de usuario mock con contraseña
     */
    private static class MockUserData {
        public User user;
        public String passwordHash;

        public MockUserData(User user, String passwordHash) {
            this.user = user;
            this.passwordHash = passwordHash;
        }
    }

    /**
     * Inicialización del servicio
     * Carga usuarios mock y configura clave JWT
     */
    @PostConstruct
    public void init() {
        logger.info("Initializing Authentication Service (Mock Mode)");
        
        try {
            // Cargar usuarios mock desde JSON
            loadMockUsers();
            logger.info("Loaded {} mock users from {}", mockUsers.size(), authProperties.getUsersFile());
            
            // Configurar clave de firma JWT
            initializeJwtKey();
            logger.info("JWT signing key initialized successfully");
            
            // Log de configuración
            logger.debug("JWT issuer: {}", authProperties.getJwt().getIssuer());
            logger.debug("JWT audience: {}", authProperties.getJwt().getAudience());
            logger.debug("JWT expiration: {} seconds", authProperties.getJwt().getExpiration());
            logger.debug("Allowed roles: {}", Arrays.toString(authProperties.getRoles()));
            
            logger.warn("Running in MOCK authentication mode - not suitable for production!");
            
        } catch (Exception e) {
            logger.error("Failed to initialize Authentication Service: {}", e.getMessage(), e);
            throw new RuntimeException("Authentication Service initialization failed", e);
        }
    }

    /**
     * Valida credenciales de usuario contra el archivo mock
     * 
     * @param username Nombre de usuario
     * @param password Contraseña en texto plano
     * @return Usuario autenticado o null si falla
     */
    public User authenticateUser(String username, String password) {
        if (username == null || username.isBlank()) {
            logger.warn("Authentication failed: username is null or empty");
            return null;
        }
        
        if (password == null || password.isBlank()) {
            logger.warn("Authentication failed: password is null or empty");
            return null;
        }
        
        logger.debug("Attempting to authenticate user: {}", username);
        
        // Normalizar username
        String normalizedUsername = username.trim().toLowerCase();
        
        // Buscar usuario en mock
        MockUserData userData = mockUsers.get(normalizedUsername);
        
        if (userData == null) {
            logger.warn("Authentication failed: user not found: {}", username);
            return null;
        }
        
        // Verificar contraseña con BCrypt
        if (!passwordEncoder.matches(password, userData.passwordHash)) {
            logger.warn("Authentication failed: invalid password for user: {}", username);
            return null;
        }
        
        // Verificar que el usuario esté activo
        if (!userData.user.active()) {
            logger.warn("Authentication failed: user is inactive: {}", username);
            return null;
        }
        
        logger.info("User authenticated successfully: {} (role: {})", username, userData.user.role());
        
        // Actualizar último login
        User authenticatedUser = userData.user.withUpdatedLastLogin();
        userData.user = authenticatedUser; // Actualizar en cache
        
        return authenticatedUser;
    }

    /**
     * Genera un token JWT para el usuario autenticado
     * 
     * @param user Usuario autenticado
     * @return JWT token como String
     * @throws IllegalArgumentException si el usuario es null
     */
    public String generateJwtToken(User user) {
        if (user == null) {
            throw new IllegalArgumentException("User cannot be null for JWT generation");
        }
        
        logger.debug("Generating JWT token for user: {}", user.username());
        
        Date now = new Date();
        Date expiration = new Date(now.getTime() + (authProperties.getJwt().getExpiration() * 1000L));
        
        String token = Jwts.builder()
            .subject(user.username())
            .claim("userId", user.id())
            .claim("email", user.email())
            .claim("fullName", user.fullName())
            .claim("role", user.role().name())
            .claim("department", user.department())
            .issuer(authProperties.getJwt().getIssuer())
            .audience().add(authProperties.getJwt().getAudience()).and()
            .issuedAt(now)
            .expiration(expiration)
            .signWith(jwtSigningKey)
            .compact();
        
        logger.info("JWT token generated for user: {} (expires: {})", user.username(), expiration);
        
        return token;
    }

    /**
     * Valida un token JWT y extrae el usuario
     * 
     * @param token Token JWT a validar
     * @return Usuario asociado al token o null si inválido
     */
    public User validateJwtToken(String token) {
        if (token == null || token.isBlank()) {
            logger.warn("JWT validation failed: token is null or empty");
            return null;
        }
        
        logger.debug("Validating JWT token");
        
        try {
            // Parsear y validar el token
            Claims claims = Jwts.parser()
                .verifyWith(jwtSigningKey)
                .requireIssuer(authProperties.getJwt().getIssuer())
                .requireAudience(authProperties.getJwt().getAudience())
                .build()
                .parseSignedClaims(token)
                .getPayload();
            
            // Extraer información del usuario
            String username = claims.getSubject();
            String userId = claims.get("userId", String.class);
            String email = claims.get("email", String.class);
            String fullName = claims.get("fullName", String.class);
            String roleStr = claims.get("role", String.class);
            String department = claims.get("department", String.class);
            
            UserRole role = UserRole.valueOf(roleStr);
            
            // Buscar usuario en cache para verificar que sigue activo
            MockUserData userData = mockUsers.get(username.toLowerCase());
            
            if (userData == null) {
                logger.warn("JWT validation failed: user not found in cache: {}", username);
                return null;
            }
            
            if (!userData.user.active()) {
                logger.warn("JWT validation failed: user is no longer active: {}", username);
                return null;
            }
            
            logger.debug("JWT token validated successfully for user: {}", username);
            
            // Retornar el usuario desde el cache (con datos actualizados)
            return userData.user;
            
        } catch (JwtException e) {
            logger.warn("JWT validation failed: {}", e.getMessage());
            return null;
        } catch (Exception e) {
            logger.error("Unexpected error validating JWT: {}", e.getMessage(), e);
            return null;
        }
    }

    /**
     * Extrae el username del token JWT sin validar completamente
     * Útil para logging y auditoría
     * 
     * @param token Token JWT
     * @return Username o null si no se puede extraer
     */
    public String extractUsername(String token) {
        if (token == null || token.isBlank()) {
            return null;
        }
        
        try {
            Claims claims = Jwts.parser()
                .verifyWith(jwtSigningKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
            
            return claims.getSubject();
        } catch (Exception e) {
            logger.debug("Failed to extract username from token: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Verifica si un usuario tiene un rol específico
     * 
     * @param user Usuario a verificar
     * @param role Rol requerido (VOTER, ADMIN, AUDITOR)
     * @return true si tiene el rol
     */
    public boolean hasRole(User user, UserRole role) {
        if (user == null || role == null) {
            return false;
        }
        
        boolean result = user.role() == role;
        logger.debug("Role check for user {}: has role {}? {}", user.username(), role, result);
        
        return result;
    }

    /**
     * Verifica si un usuario tiene uno de varios roles posibles
     * 
     * @param user Usuario a verificar
     * @param roles Roles permitidos
     * @return true si tiene alguno de los roles
     */
    public boolean hasAnyRole(User user, UserRole... roles) {
        if (user == null || roles == null || roles.length == 0) {
            return false;
        }
        
        for (UserRole role : roles) {
            if (user.role() == role) {
                logger.debug("User {} has role {}", user.username(), role);
                return true;
            }
        }
        
        logger.debug("User {} does not have any of the required roles", user.username());
        return false;
    }

    /**
     * Busca un usuario por username
     * 
     * @param username Username a buscar
     * @return Usuario encontrado o null
     */
    public User findUserByUsername(String username) {
        if (username == null || username.isBlank()) {
            return null;
        }
        
        MockUserData userData = mockUsers.get(username.trim().toLowerCase());
        return userData != null ? userData.user : null;
    }

    /**
     * Busca un usuario por ID
     * 
     * @param userId ID del usuario
     * @return Usuario encontrado o null
     */
    public User findUserById(String userId) {
        if (userId == null || userId.isBlank()) {
            return null;
        }
        
        return mockUsers.values().stream()
            .map(ud -> ud.user)
            .filter(u -> u.id().equals(userId))
            .findFirst()
            .orElse(null);
    }

    /**
     * Obtiene todos los usuarios activos
     * 
     * @return Lista de usuarios activos
     */
    public List<User> getAllActiveUsers() {
        return mockUsers.values().stream()
            .map(ud -> ud.user)
            .filter(User::active)
            .toList();
    }

    /**
     * Obtiene usuarios por rol
     * 
     * @param role Rol a filtrar
     * @return Lista de usuarios con ese rol
     */
    public List<User> getUsersByRole(UserRole role) {
        if (role == null) {
            return Collections.emptyList();
        }
        
        return mockUsers.values().stream()
            .map(ud -> ud.user)
            .filter(u -> u.role() == role)
            .toList();
    }

    /**
     * Carga usuarios desde el archivo JSON mock
     * 
     * @throws IOException si falla la carga del archivo
     */
    private void loadMockUsers() throws IOException {
        logger.debug("Loading mock users from: {}", authProperties.getUsersFile());
        
        Path usersFilePath = Paths.get(authProperties.getUsersFile());
        
        if (!Files.exists(usersFilePath)) {
            throw new IOException("Users file not found: " + usersFilePath);
        }
        
        // Leer archivo JSON
        String jsonContent = Files.readString(usersFilePath, StandardCharsets.UTF_8);
        
        // Parsear como array de JsonNode para extraer password
        JsonNode usersArray = objectMapper.readTree(jsonContent);
        
        mockUsers.clear();
        
        for (JsonNode userNode : usersArray) {
            try {
                // Extraer password hash
                String passwordHash = userNode.get("password").asText();
                
                // Crear objeto User (sin password)
                String id = userNode.get("id").asText();
                String username = userNode.get("username").asText();
                String email = userNode.get("email").asText();
                String fullName = userNode.get("fullName").asText();
                UserRole role = UserRole.valueOf(userNode.get("role").asText());
                String department = userNode.has("department") && !userNode.get("department").isNull() 
                    ? userNode.get("department").asText() : null;
                boolean active = userNode.get("active").asBoolean();
                
                LocalDateTime lastLogin = null;
                if (userNode.has("lastLogin") && !userNode.get("lastLogin").isNull()) {
                    lastLogin = LocalDateTime.parse(userNode.get("lastLogin").asText());
                }
                
                LocalDateTime createdAt = LocalDateTime.parse(userNode.get("createdAt").asText());
                
                User user = new User(id, username, email, fullName, role, department, active, lastLogin, createdAt);
                
                // Almacenar con password hash
                mockUsers.put(username.toLowerCase(), new MockUserData(user, passwordHash));
                
                logger.debug("Loaded user: {} (role: {}, active: {})", username, role, active);
                
            } catch (Exception e) {
                logger.error("Failed to parse user from JSON: {}", e.getMessage());
            }
        }
        
        if (mockUsers.isEmpty()) {
            logger.warn("No users loaded from file!");
        }
    }

    /**
     * Inicializa la clave de firma JWT desde la configuración
     */
    private void initializeJwtKey() {
        String secret = authProperties.getJwt().getSecret();
        
        if (secret == null || secret.length() < 32) {
            logger.warn("JWT secret is too short, using default (NOT SECURE for production)");
            secret = "mock-secret-key-for-development-only-min-256-bits-required-for-hs256";
        }
        
        // Crear clave HMAC SHA-256
        this.jwtSigningKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        
        logger.debug("JWT signing key initialized with HS256 algorithm");
    }
}