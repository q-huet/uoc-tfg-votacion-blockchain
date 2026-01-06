package es.tfg.votacion.service;

import es.tfg.votacion.config.AuthProperties;
import es.tfg.votacion.model.User;
import es.tfg.votacion.model.UserRole;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests para AuthService
 * 
 * Verifica:
 * - Carga de usuarios mock desde JSON
 * - Autenticación con BCrypt
 * - Generación de JWT tokens
 * - Validación de JWT tokens
 * - Gestión de roles
 * - Búsqueda de usuarios
 * 
 * @author Enrique Huet Adrover
 * @version 1.0
 */
@SpringBootTest
@TestPropertySource(properties = {
    "auth.users-file=src/main/resources/mock/users.json",
    "auth.jwt.secret=test-secret-key-for-jwt-signing-must-be-at-least-256-bits-long",
    "auth.jwt.expiration=3600",
    "auth.jwt.issuer=votacion-test",
    "auth.jwt.audience=test-system"
})
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class AuthServiceTest {

    @Autowired
    private AuthService authService;

    @Autowired
    private AuthProperties authProperties;

    private static final String VALID_USERNAME = "test.user";
    private static final String VALID_PASSWORD = "password123";
    private static final String ADMIN_USERNAME = "admin";
    private static final String AUDITOR_USERNAME = "auditor";
    private static final String INACTIVE_USER = "lucia.garcia";

    @Test
    @Order(1)
    @DisplayName("Auth properties should be injected correctly")
    void authPropertiesShouldBeInjected() {
        assertNotNull(authProperties, "Auth properties should be injected");
        assertNotNull(authProperties.getUsersFile(), "Users file should be configured");
        assertNotNull(authProperties.getJwt().getSecret(), "JWT secret should be configured");
        
        assertEquals("votacion-test", authProperties.getJwt().getIssuer());
        assertEquals("test-system", authProperties.getJwt().getAudience());
        assertEquals(3600, authProperties.getJwt().getExpiration());
    }

    @Test
    @Order(2)
    @DisplayName("Auth service should initialize successfully")
    void authServiceShouldInitialize() {
        assertNotNull(authService, "Auth service should be initialized");
        
        // Verificar que se cargaron usuarios
        List<User> activeUsers = authService.getAllActiveUsers();
        assertNotNull(activeUsers);
        assertTrue(activeUsers.size() > 0, "Should have loaded users from JSON");
    }

    @Test
    @Order(3)
    @DisplayName("Should authenticate user with valid credentials")
    void shouldAuthenticateUserWithValidCredentials() {
        // Act
        User user = authService.authenticateUser(VALID_USERNAME, VALID_PASSWORD);
        
        // Assert
        assertNotNull(user, "User should be authenticated");
        assertEquals(VALID_USERNAME, user.username());
        assertEquals(UserRole.VOTER, user.role());
        assertTrue(user.active());
        assertNotNull(user.lastLogin(), "Last login should be updated after authentication");
    }

    @Test
    @Order(4)
    @DisplayName("Should fail authentication with invalid password")
    void shouldFailAuthenticationWithInvalidPassword() {
        // Act
        User user = authService.authenticateUser(VALID_USERNAME, "wrong-password");
        
        // Assert
        assertNull(user, "Authentication should fail with wrong password");
    }

    @Test
    @Order(5)
    @DisplayName("Should fail authentication with non-existent user")
    void shouldFailAuthenticationWithNonExistentUser() {
        // Act
        User user = authService.authenticateUser("non.existent.user", VALID_PASSWORD);
        
        // Assert
        assertNull(user, "Authentication should fail for non-existent user");
    }

    @Test
    @Order(6)
    @DisplayName("Should fail authentication for inactive user")
    void shouldFailAuthenticationForInactiveUser() {
        // Act
        User user = authService.authenticateUser(INACTIVE_USER, VALID_PASSWORD);
        
        // Assert
        assertNull(user, "Authentication should fail for inactive user");
    }

    @Test
    @Order(7)
    @DisplayName("Should fail authentication with null credentials")
    void shouldFailAuthenticationWithNullCredentials() {
        assertNull(authService.authenticateUser(null, VALID_PASSWORD));
        assertNull(authService.authenticateUser(VALID_USERNAME, null));
        assertNull(authService.authenticateUser(null, null));
    }

    @Test
    @Order(8)
    @DisplayName("Should fail authentication with empty credentials")
    void shouldFailAuthenticationWithEmptyCredentials() {
        assertNull(authService.authenticateUser("", VALID_PASSWORD));
        assertNull(authService.authenticateUser(VALID_USERNAME, ""));
        assertNull(authService.authenticateUser("   ", VALID_PASSWORD));
    }

    @Test
    @Order(9)
    @DisplayName("Should generate valid JWT token")
    void shouldGenerateValidJwtToken() {
        // Arrange
        User user = authService.authenticateUser(VALID_USERNAME, VALID_PASSWORD);
        assertNotNull(user);
        
        // Act
        String token = authService.generateJwtToken(user);
        
        // Assert
        assertNotNull(token, "JWT token should be generated");
        assertFalse(token.isBlank(), "JWT token should not be blank");
        assertTrue(token.split("\\.").length == 3, "JWT should have 3 parts (header.payload.signature)");
    }

    @Test
    @Order(10)
    @DisplayName("Should throw exception when generating token for null user")
    void shouldThrowExceptionWhenGeneratingTokenForNullUser() {
        assertThrows(IllegalArgumentException.class, () -> {
            authService.generateJwtToken(null);
        });
    }

    @Test
    @Order(11)
    @DisplayName("Should validate JWT token correctly")
    void shouldValidateJwtTokenCorrectly() {
        // Arrange
        User originalUser = authService.authenticateUser(VALID_USERNAME, VALID_PASSWORD);
        String token = authService.generateJwtToken(originalUser);
        
        // Act
        User validatedUser = authService.validateJwtToken(token);
        
        // Assert
        assertNotNull(validatedUser, "Token should be validated successfully");
        assertEquals(originalUser.username(), validatedUser.username());
        assertEquals(originalUser.email(), validatedUser.email());
        assertEquals(originalUser.role(), validatedUser.role());
    }

    @Test
    @Order(12)
    @DisplayName("Should fail validation for invalid token")
    void shouldFailValidationForInvalidToken() {
        assertNull(authService.validateJwtToken("invalid.token.here"));
        assertNull(authService.validateJwtToken(null));
        assertNull(authService.validateJwtToken(""));
        assertNull(authService.validateJwtToken("   "));
    }

    @Test
    @Order(13)
    @DisplayName("Should extract username from token")
    void shouldExtractUsernameFromToken() {
        // Arrange
        User user = authService.authenticateUser(VALID_USERNAME, VALID_PASSWORD);
        String token = authService.generateJwtToken(user);
        
        // Act
        String extractedUsername = authService.extractUsername(token);
        
        // Assert
        assertEquals(VALID_USERNAME, extractedUsername);
    }

    @Test
    @Order(14)
    @DisplayName("Should check user roles correctly")
    void shouldCheckUserRolesCorrectly() {
        // Arrange
        User voter = authService.authenticateUser(VALID_USERNAME, VALID_PASSWORD);
        User admin = authService.authenticateUser(ADMIN_USERNAME, VALID_PASSWORD);
        User auditor = authService.authenticateUser(AUDITOR_USERNAME, VALID_PASSWORD);
        
        // Assert - VOTER
        assertTrue(authService.hasRole(voter, UserRole.VOTER));
        assertFalse(authService.hasRole(voter, UserRole.ADMIN));
        assertFalse(authService.hasRole(voter, UserRole.AUDITOR));
        
        // Assert - ADMIN
        assertTrue(authService.hasRole(admin, UserRole.ADMIN));
        assertFalse(authService.hasRole(admin, UserRole.VOTER));
        assertFalse(authService.hasRole(admin, UserRole.AUDITOR));
        
        // Assert - AUDITOR
        assertTrue(authService.hasRole(auditor, UserRole.AUDITOR));
        assertFalse(authService.hasRole(auditor, UserRole.VOTER));
        assertFalse(authService.hasRole(auditor, UserRole.ADMIN));
    }

    @Test
    @Order(15)
    @DisplayName("Should check multiple roles with hasAnyRole")
    void shouldCheckMultipleRoles() {
        // Arrange
        User voter = authService.authenticateUser(VALID_USERNAME, VALID_PASSWORD);
        User admin = authService.authenticateUser(ADMIN_USERNAME, VALID_PASSWORD);
        
        // Assert
        assertTrue(authService.hasAnyRole(voter, UserRole.VOTER, UserRole.ADMIN));
        assertTrue(authService.hasAnyRole(admin, UserRole.ADMIN, UserRole.AUDITOR));
        assertFalse(authService.hasAnyRole(voter, UserRole.ADMIN, UserRole.AUDITOR));
    }

    @Test
    @Order(16)
    @DisplayName("Should handle null in role checks")
    void shouldHandleNullInRoleChecks() {
        User user = authService.authenticateUser(VALID_USERNAME, VALID_PASSWORD);
        
        assertFalse(authService.hasRole(null, UserRole.VOTER));
        assertFalse(authService.hasRole(user, null));
        assertFalse(authService.hasAnyRole(null, UserRole.VOTER));
        assertFalse(authService.hasAnyRole(user, (UserRole[]) null));
    }

    @Test
    @Order(17)
    @DisplayName("Should find user by username")
    void shouldFindUserByUsername() {
        // Act
        User user = authService.findUserByUsername(VALID_USERNAME);
        
        // Assert
        assertNotNull(user);
        assertEquals(VALID_USERNAME, user.username());
    }

    @Test
    @Order(18)
    @DisplayName("Should return null for non-existent username")
    void shouldReturnNullForNonExistentUsername() {
        assertNull(authService.findUserByUsername("non.existent"));
        assertNull(authService.findUserByUsername(null));
        assertNull(authService.findUserByUsername(""));
    }

    @Test
    @Order(19)
    @DisplayName("Should find user by ID")
    void shouldFindUserById() {
        // Arrange
        User user = authService.findUserByUsername(VALID_USERNAME);
        assertNotNull(user);
        String userId = user.id();
        
        // Act
        User foundUser = authService.findUserById(userId);
        
        // Assert
        assertNotNull(foundUser);
        assertEquals(userId, foundUser.id());
        assertEquals(user.username(), foundUser.username());
    }

    @Test
    @Order(20)
    @DisplayName("Should get all active users")
    void shouldGetAllActiveUsers() {
        // Act
        List<User> activeUsers = authService.getAllActiveUsers();
        
        // Assert
        assertNotNull(activeUsers);
        assertTrue(activeUsers.size() > 0);
        
        // Verificar que todos están activos
        assertTrue(activeUsers.stream().allMatch(User::active));
        
        // Verificar que el usuario inactivo NO está en la lista
        assertFalse(activeUsers.stream().anyMatch(u -> u.username().equals(INACTIVE_USER)));
    }

    @Test
    @Order(21)
    @DisplayName("Should get users by role")
    void shouldGetUsersByRole() {
        // Act
        List<User> voters = authService.getUsersByRole(UserRole.VOTER);
        List<User> admins = authService.getUsersByRole(UserRole.ADMIN);
        List<User> auditors = authService.getUsersByRole(UserRole.AUDITOR);
        
        // Assert
        assertNotNull(voters);
        assertNotNull(admins);
        assertNotNull(auditors);
        
        assertTrue(voters.size() > 0, "Should have at least one voter");
        assertTrue(admins.size() > 0, "Should have at least one admin");
        assertTrue(auditors.size() > 0, "Should have at least one auditor");
        
        // Verificar que los roles son correctos
        assertTrue(voters.stream().allMatch(u -> u.role() == UserRole.VOTER));
        assertTrue(admins.stream().allMatch(u -> u.role() == UserRole.ADMIN));
        assertTrue(auditors.stream().allMatch(u -> u.role() == UserRole.AUDITOR));
    }

    @Test
    @Order(22)
    @DisplayName("Should return empty list for null role")
    void shouldReturnEmptyListForNullRole() {
        List<User> users = authService.getUsersByRole(null);
        assertNotNull(users);
        assertTrue(users.isEmpty());
    }

    @Test
    @Order(23)
    @DisplayName("JWT token should contain correct claims")
    void jwtTokenShouldContainCorrectClaims() {
        // Arrange
        User user = authService.authenticateUser(ADMIN_USERNAME, VALID_PASSWORD);
        String token = authService.generateJwtToken(user);
        
        // Act - validar y extraer
        User validatedUser = authService.validateJwtToken(token);
        
        // Assert
        assertNotNull(validatedUser);
        assertEquals(user.id(), validatedUser.id());
        assertEquals(user.username(), validatedUser.username());
        assertEquals(user.email(), validatedUser.email());
        assertEquals(user.fullName(), validatedUser.fullName());
        assertEquals(user.role(), validatedUser.role());
        assertEquals(user.department(), validatedUser.department());
    }

    @Test
    @Order(24)
    @DisplayName("Should authenticate different users independently")
    void shouldAuthenticateDifferentUsersIndependently() {
        // Act
        User user1 = authService.authenticateUser("juan.perez", VALID_PASSWORD);
        User user2 = authService.authenticateUser("maria.gonzalez", VALID_PASSWORD);
        User user3 = authService.authenticateUser(ADMIN_USERNAME, VALID_PASSWORD);
        
        // Assert
        assertNotNull(user1);
        assertNotNull(user2);
        assertNotNull(user3);
        
        assertNotEquals(user1.id(), user2.id());
        assertNotEquals(user1.id(), user3.id());
        assertNotEquals(user2.id(), user3.id());
        
        // Generate tokens independently
        String token1 = authService.generateJwtToken(user1);
        String token2 = authService.generateJwtToken(user2);
        String token3 = authService.generateJwtToken(user3);
        
        assertNotEquals(token1, token2);
        assertNotEquals(token1, token3);
        assertNotEquals(token2, token3);
        
        // Validate tokens independently
        User validated1 = authService.validateJwtToken(token1);
        User validated2 = authService.validateJwtToken(token2);
        User validated3 = authService.validateJwtToken(token3);
        
        assertEquals("juan.perez", validated1.username());
        assertEquals("maria.gonzalez", validated2.username());
        assertEquals(ADMIN_USERNAME, validated3.username());
    }

    @Test
    @Order(25)
    @DisplayName("Should handle username case-insensitivity")
    void shouldHandleUsernameCaseInsensitivity() {
        // Todos deberían autenticar al mismo usuario
        User user1 = authService.authenticateUser("test.user", VALID_PASSWORD);
        User user2 = authService.authenticateUser("TEST.USER", VALID_PASSWORD);
        User user3 = authService.authenticateUser("Test.User", VALID_PASSWORD);
        
        assertNotNull(user1);
        assertNotNull(user2);
        assertNotNull(user3);
        
        assertEquals(user1.id(), user2.id());
        assertEquals(user1.id(), user3.id());
    }
}
