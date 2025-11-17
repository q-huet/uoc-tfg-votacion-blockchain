package es.tfg.votacion.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import es.tfg.votacion.dto.LoginRequest;
import es.tfg.votacion.dto.LoginResponse;
import es.tfg.votacion.model.User;
import es.tfg.votacion.model.UserRole;
import es.tfg.votacion.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.*;

/**
 * Tests unitarios para AuthController usando MockMvc
 * 
 * Tests:
 * - Login exitoso con credenciales válidas
 * - Login fallido con credenciales inválidas
 * - Login fallido con usuario no existente
 * - Login fallido con campos vacíos
 * - Validación de token válido
 * - Validación de token inválido
 * - Obtener información de usuario autenticado
 * - Obtener información sin autenticación (401)
 * - Logout
 * 
 * @author Enrique Huet Adrover
 * @version 1.0
 * @since Java 21
 */
@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("AuthController Tests")
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthService authService;

    private User testUser;
    private String testToken;

    @BeforeEach
    void setUp() {
        testUser = new User(
            "user-001",
            "test.user",
            "test.user@empresa.com",
            "Test User",
            UserRole.VOTER,
            "Testing Department",
            Boolean.TRUE,
            LocalDateTime.now(),
            LocalDateTime.now().minusDays(30)
        );
        testToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJ0ZXN0LnVzZXIifQ.test";
    }

    // ==================== LOGIN TESTS ====================

    @Test
    @DisplayName("Login exitoso con credenciales válidas")
    void testLoginSuccess() throws Exception {
        // Given
        LoginRequest loginRequest = new LoginRequest("test.user", "password123");
        
        when(authService.authenticateUser("test.user", "password123"))
            .thenReturn(testUser);
        when(authService.generateJwtToken(testUser))
            .thenReturn(testToken);

        // When & Then
        mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.token").value(testToken))
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.expiresIn").value(3600))
                .andExpect(jsonPath("$.userInfo.id").value("user-001"))
                .andExpect(jsonPath("$.userInfo.username").value("test.user"))
                .andExpect(jsonPath("$.userInfo.email").value("test.user@empresa.com"))
                .andExpect(jsonPath("$.userInfo.fullName").value("Test User"))
                .andExpect(jsonPath("$.userInfo.role").value("VOTER"))
                .andExpect(jsonPath("$.userInfo.department").value("Testing Department"));
    }

    @Test
    @DisplayName("Login fallido con credenciales inválidas")
    void testLoginWithInvalidCredentials() throws Exception {
        // Given
        LoginRequest loginRequest = new LoginRequest("test.user", "wrongpassword");
        
        when(authService.authenticateUser("test.user", "wrongpassword"))
            .thenReturn(null);

        // When & Then
        mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.error").value("Unauthorized"))
                .andExpect(jsonPath("$.message").value("Invalid username or password"))
                .andExpect(jsonPath("$.path").value("/api/v1/auth/login"));
    }

    @Test
    @DisplayName("Login fallido con usuario no existente")
    void testLoginWithNonExistentUser() throws Exception {
        // Given
        LoginRequest loginRequest = new LoginRequest("nonexistent", "password123");
        
        when(authService.authenticateUser("nonexistent", "password123"))
            .thenReturn(null);

        // When & Then
        mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.message").value("Invalid username or password"));
    }

    @Test
    @DisplayName("Login fallido con username vacío")
    void testLoginWithEmptyUsername() throws Exception {
        // Given
        LoginRequest loginRequest = new LoginRequest("", "password123");

        // When & Then
        mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Login fallido con password vacío")
    void testLoginWithEmptyPassword() throws Exception {
        // Given
        LoginRequest loginRequest = new LoginRequest("test.user", "");

        // When & Then
        mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Login fallido con request body vacío")
    void testLoginWithEmptyBody() throws Exception {
        mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Login fallido con JSON malformado")
    void testLoginWithMalformedJson() throws Exception {
        mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{invalid json"))
                .andExpect(status().isBadRequest());
    }

    // ==================== TOKEN VALIDATION TESTS ====================

    @Test
    @DisplayName("Validación de token válido")
    void testValidateValidToken() throws Exception {
        // Given
        when(authService.validateJwtToken(testToken))
            .thenReturn(testUser);

        // When & Then
        mockMvc.perform(get("/api/v1/auth/validate")
                .header("Authorization", "Bearer " + testToken))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.valid").value(true))
                .andExpect(jsonPath("$.username").value("test.user"))
                .andExpect(jsonPath("$.email").value("test.user@empresa.com"))
                .andExpect(jsonPath("$.role").value("VOTER"))
                .andExpect(jsonPath("$.message").value("Token is valid"));
    }

    @Test
    @DisplayName("Validación de token inválido")
    void testValidateInvalidToken() throws Exception {
        // Given
        String invalidToken = "invalid.token.here";
        
        when(authService.validateJwtToken(invalidToken))
            .thenReturn(null);

        // When & Then
        mockMvc.perform(get("/api/v1/auth/validate")
                .header("Authorization", "Bearer " + invalidToken))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.valid").value(false))
                .andExpect(jsonPath("$.message").value("Invalid or expired token"));
    }

    @Test
    @DisplayName("Validación sin token (sin header Authorization)")
    void testValidateWithoutToken() throws Exception {
        mockMvc.perform(get("/api/v1/auth/validate"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.valid").value(false))
                .andExpect(jsonPath("$.message").value("No token provided"));
    }

    @Test
    @DisplayName("Validación con token vacío")
    void testValidateWithEmptyToken() throws Exception {
        mockMvc.perform(get("/api/v1/auth/validate")
                .header("Authorization", "Bearer "))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.valid").value(false))
                .andExpect(jsonPath("$.message").value("No token provided"));
    }

    @Test
    @DisplayName("Validación sin prefijo Bearer")
    void testValidateWithoutBearerPrefix() throws Exception {
        mockMvc.perform(get("/api/v1/auth/validate")
                .header("Authorization", testToken))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.valid").value(false));
    }

    // ==================== GET USER INFO TESTS ====================

    @Test
    @DisplayName("Obtener información del usuario autenticado")
    void testGetCurrentUserInfo() throws Exception {
        // Given
        when(authService.validateJwtToken(testToken))
            .thenReturn(testUser);

        // When & Then
        mockMvc.perform(get("/api/v1/auth/user")
                .header("Authorization", "Bearer " + testToken))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value("user-001"))
                .andExpect(jsonPath("$.username").value("test.user"))
                .andExpect(jsonPath("$.email").value("test.user@empresa.com"))
                .andExpect(jsonPath("$.fullName").value("Test User"))
                .andExpect(jsonPath("$.role").value("VOTER"))
                .andExpect(jsonPath("$.department").value("Testing Department"))
                .andExpect(jsonPath("$.active").value(true));
    }

    @Test
    @DisplayName("Obtener información sin token retorna 401")
    void testGetCurrentUserWithoutToken() throws Exception {
        mockMvc.perform(get("/api/v1/auth/user"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Obtener información con token inválido retorna 401")
    void testGetCurrentUserWithInvalidToken() throws Exception {
        // Given
        String invalidToken = "invalid.token.here";
        
        when(authService.validateJwtToken(invalidToken))
            .thenReturn(null);

        // When & Then
        mockMvc.perform(get("/api/v1/auth/user")
                .header("Authorization", "Bearer " + invalidToken))
                .andExpect(status().isUnauthorized());
    }

    // ==================== LOGOUT TESTS ====================

    @Test
    @DisplayName("Logout exitoso (informativo)")
    void testLogout() throws Exception {
        // Given
        when(authService.validateJwtToken(testToken))
            .thenReturn(testUser);

        // When & Then
        mockMvc.perform(post("/api/v1/auth/logout")
                .header("Authorization", "Bearer " + testToken))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value("Logout successful"))
                .andExpect(jsonPath("$.note", containsString("JWT is stateless")));
    }

    @Test
    @DisplayName("Logout sin token retorna 401")
    void testLogoutWithoutToken() throws Exception {
        mockMvc.perform(post("/api/v1/auth/logout"))
                .andExpect(status().isUnauthorized());
    }

    // ==================== ROLE TESTS ====================

    @Test
    @DisplayName("Login con usuario ADMIN retorna token con rol correcto")
    void testAdminLogin() throws Exception {
        User adminUser = new User(
                "admin-001",
                "admin.user",
                "admin.user@empresa.com",
                "Admin User",
                UserRole.ADMIN,
                "IT Department",
                Boolean.TRUE,
                LocalDateTime.now(),
                LocalDateTime.now().minusDays(30));

        when(authService.authenticateUser("admin.user", "admin123"))
                .thenReturn(adminUser);
        when(authService.generateJwtToken(adminUser))
                .thenReturn("admin.jwt.token");

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"admin.user\",\"password\":\"admin123\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("admin.jwt.token"))
                .andExpect(jsonPath("$.user.role").value("ADMIN"));

        verify(authService).authenticateUser("admin.user", "admin123");
        verify(authService).generateJwtToken(adminUser);
    }

    @Test
    @DisplayName("Login con usuario AUDITOR retorna token con rol correcto")
    void testAuditorLogin() throws Exception {
        User auditorUser = new User(
                "auditor-001",
                "auditor.user",
                "auditor.user@empresa.com",
                "Auditor User",
                UserRole.AUDITOR,
                "Audit Department",
                Boolean.TRUE,
                LocalDateTime.now(),
                LocalDateTime.now().minusDays(30));

        when(authService.authenticateUser("auditor.user", "auditor123"))
                .thenReturn(auditorUser);
        when(authService.generateJwtToken(auditorUser))
                .thenReturn("auditor.jwt.token");

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"auditor.user\",\"password\":\"auditor123\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("auditor.jwt.token"))
                .andExpect(jsonPath("$.user.role").value("AUDITOR"));

        verify(authService).authenticateUser("auditor.user", "auditor123");
        verify(authService).generateJwtToken(auditorUser);
    }

    // ==================== EDGE CASES ====================

    @Test
    @DisplayName("Login con username case-insensitive")
    void testLoginWithDifferentCase() throws Exception {
        // Given
        LoginRequest loginRequest = new LoginRequest("TEST.USER", "password123");
        
        when(authService.authenticateUser("TEST.USER", "password123"))
            .thenReturn(testUser);
        when(authService.generateJwtToken(testUser))
            .thenReturn(testToken);

        // When & Then
        mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userInfo.username").value("test.user"));
    }

    @Test
    @DisplayName("Token expiration está presente en respuesta de login")
    void testLoginResponseContainsExpiration() throws Exception {
        // Given
        LoginRequest loginRequest = new LoginRequest("test.user", "password123");
        
        when(authService.authenticateUser("test.user", "password123"))
            .thenReturn(testUser);
        when(authService.generateJwtToken(testUser))
            .thenReturn(testToken);

        // When & Then
        mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.expiresIn").isNumber())
                .andExpect(jsonPath("$.expiresIn").value(3600));
    }

    @Test
    @DisplayName("Validación de token retorna información completa del usuario")
    void testValidateTokenReturnsCompleteUserInfo() throws Exception {
        // Given
        when(authService.validateJwtToken(testToken))
            .thenReturn(testUser);

        // When & Then
        mockMvc.perform(get("/api/v1/auth/validate")
                .header("Authorization", "Bearer " + testToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valid").value(true))
                .andExpect(jsonPath("$.username").exists())
                .andExpect(jsonPath("$.email").exists())
                .andExpect(jsonPath("$.role").exists())
                .andExpect(jsonPath("$.message").exists());
    }
}
