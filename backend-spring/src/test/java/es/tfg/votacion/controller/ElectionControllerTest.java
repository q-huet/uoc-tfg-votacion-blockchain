package es.tfg.votacion.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import es.tfg.votacion.dto.VoteSubmissionRequest;
import es.tfg.votacion.model.*;
import es.tfg.votacion.service.AuthService;
import es.tfg.votacion.service.ElectionService;
import es.tfg.votacion.service.FabricService;
import es.tfg.votacion.service.StorageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.*;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.*;

/**
 * Tests unitarios para ElectionController usando MockMvc
 * 
 * Tests:
 * - Listar elecciones activas (público)
 * - Obtener detalle de elección (autenticado)
 * - Emitir voto (VOTER)
 * - Prevención de voto duplicado
 * - Cerrar elección (ADMIN)
 * - Ver resultados (ADMIN/AUDITOR)
 * - Tests de autorización por roles
 * - Tests de validación de datos
 * 
 * @author Enrique Huet Adrover
 * @version 1.0
 * @since Java 21
 */
@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("ElectionController Tests")
class ElectionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthService authService;

    @MockBean
    private ElectionService electionService;

    @MockBean
    private FabricService fabricService;

    @MockBean
    private StorageService storageService;

    private User voterUser;
    private User adminUser;
    private User auditorUser;
    private String voterToken;
    private String adminToken;
    private String auditorToken;
    private Election activeElection;
    private Election closedElection;
    private List<ElectionOption> options;

        @BeforeEach
    void setUp() {
        // Usuario votante de prueba
        voterUser = new User(
                "voter-001",
                "test.voter",
                "test.voter@empresa.com",
                "Test Voter",
                UserRole.VOTER,
                "Testing Department",
                Boolean.TRUE,
                LocalDateTime.now(),
                LocalDateTime.now().minusDays(30));

        // Usuario administrador de prueba
        adminUser = new User(
                "admin-001",
                "test.admin",
                "test.admin@empresa.com",
                "Test Admin",
                UserRole.ADMIN,
                "Admin Department",
                Boolean.TRUE,
                LocalDateTime.now(),
                LocalDateTime.now().minusDays(30));

        // Usuario auditor de prueba
        auditorUser = new User(
                "auditor-001",
                "test.auditor",
                "test.auditor@empresa.com",
                "Test Auditor",
                UserRole.AUDITOR,
                "Audit Department",
                Boolean.TRUE,
                LocalDateTime.now(),
                LocalDateTime.now().minusDays(30));

        // Tokens de prueba
        voterToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.voter.token";
        adminToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.admin.token";
        auditorToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.auditor.token";

        // Opciones de elección
        options = List.of(
                new ElectionOption("option-1", "Candidate A", "Description A", 1),
                new ElectionOption("option-2", "Candidate B", "Description B", 2),
                new ElectionOption("option-3", "Candidate C", "Description C", 3)
        );

        // Elección activa
        activeElection = new Election(
                "election-001",
                "Test Election",
                "Test Description",
                options,
                ElectionStatus.ACTIVE,
                LocalDateTime.now().minusHours(1),
                LocalDateTime.now().plusHours(23),
                "admin-001",
                LocalDateTime.now().minusDays(1),
                0,
                1,
                Boolean.FALSE,
                Boolean.TRUE,
                "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA..."
        );

        // Elección cerrada
        closedElection = new Election(
                "election-002",
                "Closed Election",
                "This election is closed",
                options,
                ElectionStatus.CLOSED,
                LocalDateTime.now().minusDays(2),
                LocalDateTime.now().minusDays(1),
                "admin-001",
                LocalDateTime.now().minusDays(3),
                40,
                1,
                Boolean.FALSE,
                Boolean.TRUE,
                "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA..."
        );
        // Mock auth service roles
        when(authService.hasRole(any(), eq(UserRole.ADMIN))).thenReturn(true);
        when(authService.hasRole(any(), eq(UserRole.VOTER))).thenReturn(true);
        when(authService.hasRole(any(), eq(UserRole.AUDITOR))).thenReturn(true);
        // Fix for varargs matching
        when(authService.hasAnyRole(any(), any())).thenReturn(true);
        when(authService.hasAnyRole(any(), eq(UserRole.ADMIN), eq(UserRole.AUDITOR))).thenReturn(true);
    }

    // ==================== GET ELECTIONS TESTS ====================

    @Test
    @DisplayName("Listar elecciones activas sin autenticación (público)")
    void testGetActiveElectionsPublic() throws Exception {
        // Given
        when(electionService.getActiveElections())
            .thenReturn(List.of(activeElection));

        // When & Then
        mockMvc.perform(get("/elections"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value("election-001"))
                .andExpect(jsonPath("$[0].title").value("Test Election"))
                .andExpect(jsonPath("$[0].status").value("active"))
                .andExpect(jsonPath("$[0].hasVoted").value(false));
    }

    @Test
    @DisplayName("Listar elecciones con autenticación muestra si usuario ha votado")
    void testGetActiveElectionsWithAuth() throws Exception {
        // Given
        when(authService.validateJwtToken(voterToken))
            .thenReturn(voterUser);
        when(electionService.getActiveElections())
            .thenReturn(List.of(activeElection));
        when(electionService.hasUserVoted("election-001", "voter-001"))
            .thenReturn(true);

        // When & Then
        mockMvc.perform(get("/elections")
                .header("Authorization", "Bearer " + voterToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].hasVoted").value(true));
    }

    @Test
    @DisplayName("Listar elecciones retorna lista vacía cuando no hay elecciones activas")
    void testGetActiveElectionsEmpty() throws Exception {
        // Given
        when(electionService.getActiveElections())
            .thenReturn(Collections.emptyList());

        // When & Then
        mockMvc.perform(get("/elections"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    // ==================== GET ELECTION BY ID TESTS ====================

    @Test
    @DisplayName("Obtener detalle de elección con autenticación")
    void testGetElectionById() throws Exception {
        // Given
        when(authService.validateJwtToken(voterToken))
            .thenReturn(voterUser);
        when(electionService.getElectionById("election-001"))
            .thenReturn(Optional.of(activeElection));
        when(electionService.hasUserVoted("election-001", "voter-001"))
            .thenReturn(false);

        // When & Then
        mockMvc.perform(get("/elections/election-001")
                .header("Authorization", "Bearer " + voterToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("election-001"))
                .andExpect(jsonPath("$.title").value("Test Election"))
                .andExpect(jsonPath("$.status").value("active"))
                .andExpect(jsonPath("$.options", hasSize(3)))
                .andExpect(jsonPath("$.hasVoted").value(false));
    }

    @Test
    @DisplayName("Obtener elección inexistente retorna 404")
    void testGetNonExistentElection() throws Exception {
        // Given
        when(authService.validateJwtToken(voterToken))
            .thenReturn(voterUser);
        when(electionService.getElectionById("non-existent"))
            .thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(get("/elections/non-existent")
                .header("Authorization", "Bearer " + voterToken))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Election not found: non-existent"));
    }

    @Test
    @DisplayName("Obtener elección sin token retorna 401")
    void testGetElectionWithoutAuth() throws Exception {
        mockMvc.perform(get("/elections/election-001"))
                .andExpect(status().isUnauthorized());
    }

    // ==================== VOTE SUBMISSION TESTS ====================

    @Test
    @DisplayName("Emitir voto exitosamente como VOTER")
    void testEmitVoteSuccess() throws Exception {
        // Given
        VoteSubmissionRequest voteRequest = new VoteSubmissionRequest(
            "election-001",
            "opt-001",
            "encrypted-payload",
            "Comentario opcional"
        );

        when(authService.validateJwtToken(voterToken))
            .thenReturn(voterUser);
        when(electionService.getElectionById("election-001"))
            .thenReturn(Optional.of(activeElection));
        when(electionService.hasUserVoted("election-001", "voter-001"))
            .thenReturn(false);
        when(storageService.storeEncrypted(eq("election-001"), any(byte[].class)))
            .thenReturn("blob-12345");
        when(fabricService.emitVote(anyString(), eq("election-001")))
            .thenReturn("1234567890abcdef");

        // When & Then
        mockMvc.perform(post("/elections/election-001/vote")
                .header("Authorization", "Bearer " + voterToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(voteRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.transactionId").value("1234567890abcdef"))
                .andExpect(jsonPath("$.electionId").value("election-001"))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.commitment").exists())
                .andExpect(jsonPath("$.blobId").value("blob-12345"))
                .andExpect(jsonPath("$.verified").value(true));

        // Verify vote was registered
        verify(electionService).registerVote("election-001", "voter-001", "ENCRYPTED");
    }

    @Test
    @DisplayName("Emitir voto sin autenticación retorna 401")
    void testEmitVoteWithoutAuth() throws Exception {
        // Given
        VoteSubmissionRequest voteRequest = new VoteSubmissionRequest(
            "election-001",
            "opt-001",
            "encrypted-payload",
            null
        );

        // When & Then
        mockMvc.perform(post("/elections/election-001/vote")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(voteRequest)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Emitir voto como ADMIN retorna 403 (rol incorrecto)")
    void testEmitVoteAsAdminForbidden() throws Exception {
        // Given
        VoteSubmissionRequest voteRequest = new VoteSubmissionRequest(
            "election-001",
            "opt-001",
            "encrypted-payload",
            null
        );

        when(authService.validateJwtToken(adminToken))
            .thenReturn(adminUser);

        // When & Then
        mockMvc.perform(post("/elections/election-001/vote")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(voteRequest)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Prevención de voto duplicado retorna 409 Conflict")
    void testPreventDuplicateVote() throws Exception {
        // Given
        VoteSubmissionRequest voteRequest = new VoteSubmissionRequest(
            "election-001",
            "opt-001",
            "encrypted-payload",
            null
        );

        when(authService.validateJwtToken(voterToken))
            .thenReturn(voterUser);
        when(electionService.getElectionById("election-001"))
            .thenReturn(Optional.of(activeElection));
        when(electionService.hasUserVoted("election-001", "voter-001"))
            .thenReturn(true);

        // When & Then
        mockMvc.perform(post("/elections/election-001/vote")
                .header("Authorization", "Bearer " + voterToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(voteRequest)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message").value("User has already voted in this election"));

        // Verify vote was NOT registered
        verify(electionService, never()).registerVote(anyString(), anyString(), anyString());
    }

    @Test
    @DisplayName("Votar en elección cerrada retorna 400")
    void testVoteInClosedElection() throws Exception {
        // Given
        VoteSubmissionRequest voteRequest = new VoteSubmissionRequest(
            "election-002",
            "opt-001",
            "encrypted-payload",
            null
        );

        when(authService.validateJwtToken(voterToken))
            .thenReturn(voterUser);
        when(electionService.getElectionById("election-002"))
            .thenReturn(Optional.of(closedElection));

        // When & Then
        mockMvc.perform(post("/elections/election-002/vote")
                .header("Authorization", "Bearer " + voterToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(voteRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Election is not active for voting"));
    }

    @Test
    @DisplayName("Votar con opción inválida retorna 400")
    void testVoteWithInvalidOption() throws Exception {
        // Given
        VoteSubmissionRequest voteRequest = new VoteSubmissionRequest(
            "election-001",
            "invalid-option",
            null,
            null
        );

        when(authService.validateJwtToken(voterToken))
            .thenReturn(voterUser);
        when(electionService.getElectionById("election-001"))
            .thenReturn(Optional.of(activeElection));
        when(electionService.hasUserVoted("election-001", "voter-001"))
            .thenReturn(false);

        // When & Then
        mockMvc.perform(post("/elections/election-001/vote")
                .header("Authorization", "Bearer " + voterToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(voteRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Invalid option ID or missing encrypted payload"));
    }

    @Test
    @DisplayName("Votar con campos vacíos retorna 400")
    void testVoteWithEmptyFields() throws Exception {
        // Given
        VoteSubmissionRequest voteRequest = new VoteSubmissionRequest("", "", "", null);

        when(authService.validateJwtToken(voterToken))
            .thenReturn(voterUser);

        // When & Then
        mockMvc.perform(post("/elections/election-001/vote")
                .header("Authorization", "Bearer " + voterToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(voteRequest)))
                .andExpect(status().isBadRequest());
    }

    // ==================== CLOSE ELECTION TESTS ====================

    @Test
    @DisplayName("Cerrar elección como ADMIN exitosamente")
    void testCloseElectionAsAdmin() throws Exception {
        // Given
        when(authService.validateJwtToken(adminToken))
            .thenReturn(adminUser);
        when(electionService.getElectionById("election-001"))
            .thenReturn(Optional.of(activeElection));
        
        // Mock return value
        when(electionService.closeElection(eq("election-001"), anyString()))
            .thenReturn(closedElection);

        es.tfg.votacion.dto.CloseElectionRequest closeRequest = new es.tfg.votacion.dto.CloseElectionRequest("fake-private-key");

        // When & Then
        mockMvc.perform(post("/elections/election-001/close")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(closeRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("closed"));

        verify(electionService).closeElection(eq("election-001"), anyString());
    }

    @Test
    @DisplayName("Cerrar elección sin autenticación retorna 401")
    void testCloseElectionWithoutAuth() throws Exception {
        mockMvc.perform(post("/elections/election-001/close"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Cerrar elección como VOTER retorna 403")
    void testCloseElectionAsVoterForbidden() throws Exception {
        // Given
        when(authService.validateJwtToken(voterToken))
            .thenReturn(voterUser);

        // When & Then
        mockMvc.perform(post("/elections/election-001/close")
                .header("Authorization", "Bearer " + voterToken))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Cerrar elección inexistente retorna 404")
    void testCloseNonExistentElection() throws Exception {
        // Given
        when(authService.validateJwtToken(adminToken))
            .thenReturn(adminUser);
        when(electionService.getElectionById("non-existent"))
            .thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(post("/elections/non-existent/close")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"privateKey\":\"fake-private-key\"}"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Election not found: non-existent"));
    }

    @Test
    @DisplayName("Cerrar elección ya cerrada retorna 400")
    void testCloseAlreadyClosedElection() throws Exception {
        // Given
        when(authService.validateJwtToken(adminToken))
            .thenReturn(adminUser);
        when(electionService.getElectionById("election-002"))
            .thenReturn(Optional.of(closedElection));

        // When & Then
        mockMvc.perform(post("/elections/election-002/close")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"privateKey\":\"fake-private-key\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Election is not active"));
    }

    // ==================== GET RESULTS TESTS ====================

    @Test
    @DisplayName("Ver resultados como ADMIN")
    void testGetResultsAsAdmin() throws Exception {
        // Given
        Map<String, Integer> results = Map.of(
            "opt-001", 25,
            "opt-002", 15
        );

        when(authService.validateJwtToken(adminToken))
            .thenReturn(adminUser);
        when(electionService.getElectionById("election-002"))
            .thenReturn(Optional.of(closedElection));
        when(electionService.getElectionResults("election-002"))
            .thenReturn(results);

        // When & Then
        mockMvc.perform(get("/elections/election-002/results")
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Closed Election"))
                .andExpect(jsonPath("$.status").value("closed"))
                .andExpect(jsonPath("$.totalVotes").value(40))
                .andExpect(jsonPath("$.results", hasSize(3)))
                .andExpect(jsonPath("$.results[0].votes").isNumber())
                .andExpect(jsonPath("$.results[0].percentage").isNumber());
    }

    @Test
    @DisplayName("Ver resultados como AUDITOR")
    void testGetResultsAsAuditor() throws Exception {
        // Given
        Map<String, Integer> results = Map.of("opt-001", 10);

        when(authService.validateJwtToken(auditorToken))
            .thenReturn(auditorUser);
        when(electionService.getElectionById("election-002"))
            .thenReturn(Optional.of(closedElection));
        when(electionService.getElectionResults("election-002"))
            .thenReturn(results);

        // When & Then
        mockMvc.perform(get("/elections/election-002/results")
                .header("Authorization", "Bearer " + auditorToken))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Ver resultados sin autenticación retorna 401")
    void testGetResultsWithoutAuth() throws Exception {
        mockMvc.perform(get("/elections/election-002/results"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Ver resultados como VOTER retorna 403")
    void testGetResultsAsVoterForbidden() throws Exception {
        // Given
        when(authService.validateJwtToken(voterToken))
            .thenReturn(voterUser);

        // When & Then
        mockMvc.perform(get("/elections/election-002/results")
                .header("Authorization", "Bearer " + voterToken))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Ver resultados de elección activa retorna 400")
    void testGetResultsOfActiveElection() throws Exception {
        // Given
        when(authService.validateJwtToken(adminToken))
            .thenReturn(adminUser);
        when(electionService.getElectionById("election-001"))
            .thenReturn(Optional.of(activeElection));

        // When & Then
        mockMvc.perform(get("/elections/election-001/results")
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Election must be closed to view results"));
    }

    @Test
    @DisplayName("Ver resultados de elección inexistente retorna 404")
    void testGetResultsOfNonExistentElection() throws Exception {
        // Given
        when(authService.validateJwtToken(adminToken))
            .thenReturn(adminUser);
        when(electionService.getElectionById("non-existent"))
            .thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(get("/elections/non-existent/results")
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNotFound());
    }

    // ==================== INTEGRATION TESTS ====================

    @Test
    @DisplayName("Flujo completo: listar → obtener → votar")
    void testCompleteVotingFlow() throws Exception {
        // 1. List elections
        when(electionService.getActiveElections())
            .thenReturn(List.of(activeElection));

        mockMvc.perform(get("/elections"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));

        // 2. Get election details
        when(authService.validateJwtToken(voterToken))
            .thenReturn(voterUser);
        when(electionService.getElectionById("election-001"))
            .thenReturn(Optional.of(activeElection));
        when(electionService.hasUserVoted("election-001", "voter-001"))
            .thenReturn(false);

        mockMvc.perform(get("/elections/election-001")
                .header("Authorization", "Bearer " + voterToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.hasVoted").value(false));

        // 3. Submit vote
        VoteSubmissionRequest voteRequest = new VoteSubmissionRequest(
            "election-001",
            "opt-001",
            "encrypted-payload",
            null
        );

        when(storageService.storeEncrypted(eq("election-001"), any(byte[].class)))
            .thenReturn("blob-12345");
        when(fabricService.emitVote(anyString(), eq("election-001")))
            .thenReturn("1234567890abcdef");

        mockMvc.perform(post("/elections/election-001/vote")
                .header("Authorization", "Bearer " + voterToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(voteRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.verified").value(true));
    }
}
