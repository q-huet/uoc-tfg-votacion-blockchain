package es.tfg.votacion.service;

import es.tfg.votacion.config.FabricProperties;
import es.tfg.votacion.model.ElectionStatus;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test para FabricService - Verificación de funcionalidad mock
 * 
 * @author Enrique Huet Adrover
 * @version 1.0
 * @since Java 21
 */
@SpringBootTest
@TestPropertySource(properties = {
    "fabric.connection-profile=/tmp/mock-connection.json",
    "fabric.wallet-path=/tmp/mock-wallet",
    "fabric.user=mockUser",
    "fabric.channel=mockchannel", 
    "fabric.chaincode=mockcc"
})
class FabricServiceTest {

    @Autowired
    private FabricService fabricService;

    @Autowired
    private FabricProperties fabricProperties;

    @Test
    void fabricPropertiesShouldBeInjected() {
        assertNotNull(fabricService);
        assertNotNull(fabricProperties);
        assertEquals("mockUser", fabricProperties.getUser());
        assertEquals("mockchannel", fabricProperties.getChannel());
        assertEquals("mockcc", fabricProperties.getChaincode());
    }

    @Test
    void shouldNotBeConnectedInMockMode() {
        // En modo mock, isConnected() debe devolver false para activar fallbacks
        assertFalse(fabricService.isConnected());
    }

    @Test
    void emitVoteShouldReturnMockTransactionId() {
        String electionId = "test-election-001";
        String commitment = "abc123def456";

        String transactionId = fabricService.emitVote(commitment, electionId);

        assertNotNull(transactionId);
        assertTrue(transactionId.startsWith("MOCK-TX-"));
        assertTrue(transactionId.contains(electionId));
        
        // Verificar que el formato es correcto: MOCK-TX-{electionId}-{timestamp}
        assertTrue(transactionId.matches("MOCK-TX-.*-\\d+"));
    }

    @Test
    void closeElectionShouldReturnMockResult() {
        String electionId = "test-election-002";

        Map<String, Object> result = fabricService.closeElection(electionId);

        assertNotNull(result);
        assertEquals(electionId, result.get("electionId"));
        assertEquals("CLOSED", result.get("status"));
        assertEquals(true, result.get("mockMode"));
        assertNotNull(result.get("timestamp"));
    }

    @Test
    void countVotesShouldReturnMockResults() {
        String electionId = "test-election-003";

        Map<String, Object> results = fabricService.countVotes(electionId);

        assertNotNull(results);
        assertEquals(electionId, results.get("electionId"));
        assertEquals(0, results.get("totalVotes"));
        assertEquals(true, results.get("mockMode"));
        assertNotNull(results.get("timestamp"));
    }

    @Test
    void queryElectionStatusShouldReturnActiveStatus() {
        String electionId = "test-election-004";

        ElectionStatus status = fabricService.queryElectionStatus(electionId);

        assertNotNull(status);
        assertEquals(ElectionStatus.ACTIVE, status);
    }

    @Test
    void verifyVoteTransactionShouldValidateMockTransactions() {
        // Test con ID válido de mock
        String validMockId = "MOCK-TX-12345";
        assertTrue(fabricService.verifyVoteTransaction(validMockId));

        // Test con ID válido regular
        String validTxId = "TX-election-001-67890";
        assertTrue(fabricService.verifyVoteTransaction(validTxId));

        // Test con ID inválido
        String invalidId = "INVALID-ID";
        assertFalse(fabricService.verifyVoteTransaction(invalidId));

        // Test con null
        assertFalse(fabricService.verifyVoteTransaction(null));
    }

    @Test
    void integrationTestEmitAndVerifyVote() {
        String electionId = "integration-test-election";
        String commitment = "integration-test-commitment";

        // Emitir voto
        String transactionId = fabricService.emitVote(commitment, electionId);
        assertNotNull(transactionId);
        assertTrue(transactionId.startsWith("MOCK-TX-"));

        // Verificar voto
        assertTrue(fabricService.verifyVoteTransaction(transactionId));

        // Consultar estado de elección
        ElectionStatus status = fabricService.queryElectionStatus(electionId);
        assertEquals(ElectionStatus.ACTIVE, status);

        // Contar votos
        Map<String, Object> voteCount = fabricService.countVotes(electionId);
        assertEquals(electionId, voteCount.get("electionId"));
        assertEquals(true, voteCount.get("mockMode"));
    }

    @Test
    void closeGatewayShouldNotThrowException() {
        // Este test verifica que el método cleanup no lance excepciones
        assertDoesNotThrow(() -> {
            fabricService.closeGateway();
        });
    }
}