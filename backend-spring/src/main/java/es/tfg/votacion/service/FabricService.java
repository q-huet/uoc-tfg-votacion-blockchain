package es.tfg.votacion.service;

import es.tfg.votacion.config.FabricProperties;
import es.tfg.votacion.model.ElectionStatus;
import org.hyperledger.fabric.client.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.util.Map;
import java.util.HashMap;

/**
 * Servicio para comunicación con Hyperledger Fabric
 * 
 * Gestiona:
 * - Conexión con la red Fabric usando Gateway SDK
 * - Invocación de chaincode para registro de votos
 * - Consultas de estado del ledger
 * - Manejo de timeouts y reintentos con backoff exponencial
 * 
 * @author Enrique Huet Adrover
 * @version 1.0
 * @since Java 21
 */
@Service
public class FabricService {

    private static final Logger logger = LoggerFactory.getLogger(FabricService.class);

    @Autowired
    private FabricProperties fabricProperties;

    private Gateway gateway;
    private Contract contract;
    private boolean mockMode = true; // Para el PoC, trabajamos en modo mock

    /**
     * Inicializa la conexión con la red Hyperledger Fabric después de construir el bean
     */
    @PostConstruct
    public void initializeConnection() {
        try {
            logger.info("Initializing Hyperledger Fabric Gateway connection");
            initGateway();
            logger.info("Successfully connected to Hyperledger Fabric network");
        } catch (Exception e) {
            logger.error("Failed to initialize Fabric connection: {}", e.getMessage(), e);
            // En un entorno de producción, podríamos lanzar una excepción aquí
            // Para el PoC, continuamos sin conexión blockchain
        }
    }

    /**
     * Cierra la conexión con el Gateway al destruir el bean
     */
    @PreDestroy
    public void cleanup() {
        closeGateway();
    }

    /**
     * Inicializa la conexión con la red Hyperledger Fabric
     * 
     * @throws Exception si no se puede establecer la conexión
     */
    private void initGateway() throws Exception {
        try {
            logger.info("Attempting to connect to Fabric network...");
            logger.debug("Connection profile: {}", fabricProperties.getConnectionProfile());
            logger.debug("Wallet path: {}", fabricProperties.getWalletPath());
            logger.debug("User: {}", fabricProperties.getUser());
            logger.debug("Channel: {}", fabricProperties.getChannel());
            logger.debug("Chaincode: {}", fabricProperties.getChaincode());
            
            // Para el PoC, creamos una implementación mock que simula la conexión
            // En un entorno real, aquí estaría la implementación completa del Gateway SDK
            
            // Por ahora, marcamos como "conectado" pero sin conexión real
            // Esto permite que el resto de la aplicación funcione sin necesidad de tener
            // Hyperledger Fabric corriendo localmente
            
            logger.warn("Using MOCK Fabric connection for PoC development");
            logger.warn("To use real Fabric connection, implement proper Gateway initialization");
            
            // Simulamos conexión exitosa
            // En producción: cargar wallet, verificar identidad, crear Gateway, etc.
            
        } catch (Exception e) {
            logger.error("Failed to initialize Fabric gateway: {}", e.getMessage());
            throw new RuntimeException("Failed to connect to Fabric network", e);
        }
    }

    /**
     * Emite un voto en la blockchain registrando el commitment
     * 
     * @param commitment Hash del voto cifrado
     * @param electionId ID de la elección
     * @return ID de transacción en el ledger
     * @throws RuntimeException si la operación falla
     */
    public String emitVote(String commitment, String electionId) {
        logger.info("Emitting vote commitment to blockchain - Election: {}, Commitment: {}", electionId, commitment);
        
        if (!isConnected()) {
            logger.warn("No blockchain connection available, returning mock transaction ID");
            return "MOCK-TX-" + electionId + "-" + System.currentTimeMillis();
        }

        return executeWithRetry(() -> {
            try {
                // Para el PoC, simulamos la invocación del chaincode
                // En producción real, aquí se invocaría el chaincode de Fabric
                
                String mockTransactionId = "TX-" + electionId + "-" + System.currentTimeMillis();
                
                logger.info("MOCK: Vote emission simulated successfully");
                logger.debug("MOCK Transaction details - Election: {}, Commitment: {}, TX-ID: {}", 
                    electionId, commitment, mockTransactionId);
                
                // En implementación real:
                // byte[] result = contract.submitTransaction("EmitVote", electionId, commitment, timestamp);
                // String transactionId = extractTransactionId(result);
                
                return mockTransactionId;
                
            } catch (Exception e) {
                logger.error("Failed to emit vote to blockchain: {}", e.getMessage());
                throw new RuntimeException("Failed to submit vote transaction", e);
            }
        }, "emitVote");
    }

    /**
     * Cierra una elección en la blockchain
     * 
     * @param electionId ID de la elección a cerrar
     * @return Resultado del cierre con estadísticas
     */
    public Map<String, Object> closeElection(String electionId) {
        logger.info("Closing election on blockchain: {}", electionId);
        
        if (!isConnected()) {
            logger.warn("No blockchain connection available, returning mock result");
            return createMockElectionResult(electionId);
        }

        return executeWithRetry(() -> {
            try {
                // MOCK: Simulación de cierre de elección
                logger.info("MOCK: Election closure simulated successfully");
                
                // En implementación real:
                // byte[] result = contract.evaluateTransaction("CloseElection", electionId);
                // String resultJson = new String(result);
                
                Map<String, Object> electionResult = new HashMap<>();
                electionResult.put("electionId", electionId);
                electionResult.put("status", "CLOSED");
                electionResult.put("timestamp", System.currentTimeMillis());
                electionResult.put("mockMode", true);
                electionResult.put("message", "Election closed successfully (MOCK mode)");
                
                return electionResult;
                
            } catch (Exception e) {
                logger.error("Failed to close election on blockchain: {}", e.getMessage());
                throw new RuntimeException("Failed to close election", e);
            }
        }, "closeElection");
    }

    /**
     * Obtiene el recuento de votos desde la blockchain
     * 
     * @param electionId ID de la elección
     * @return Resultados agregados de la votación
     */
    public Map<String, Object> countVotes(String electionId) {
        logger.info("Counting votes from blockchain for election: {}", electionId);
        
        if (!isConnected()) {
            logger.warn("No blockchain connection available, returning mock results");
            return createMockVoteResults(electionId);
        }

        return executeWithRetry(() -> {
            try {
                // MOCK: Simulación de recuento de votos
                logger.debug("MOCK: Vote counting simulated for election: {}", electionId);
                
                // En implementación real:
                // byte[] result = contract.evaluateTransaction("CountVotes", electionId);
                // String resultJson = new String(result);
                
                Map<String, Object> voteResults = new HashMap<>();
                voteResults.put("electionId", electionId);
                voteResults.put("totalVotes", 0);
                voteResults.put("timestamp", System.currentTimeMillis());
                voteResults.put("mockMode", true);
                voteResults.put("message", "Vote count simulated (MOCK mode)");
                
                return voteResults;
                
            } catch (Exception e) {
                logger.error("Failed to count votes from blockchain: {}", e.getMessage());
                throw new RuntimeException("Failed to count votes", e);
            }
        }, "countVotes");
    }

    /**
     * Consulta el estado de una elección en la blockchain
     * 
     * @param electionId ID de la elección
     * @return Estado actual de la elección
     */
    public ElectionStatus queryElectionStatus(String electionId) {
        logger.debug("Querying election status: {}", electionId);
        
        if (!isConnected()) {
            logger.warn("No blockchain connection available, returning default status");
            return ElectionStatus.ACTIVE; // En modo mock, asumimos que las elecciones están activas
        }

        return executeWithRetry(() -> {
            try {
                // MOCK: Simulación de consulta de estado
                logger.debug("MOCK: Election status query simulated for: {}", electionId);
                
                // En implementación real:
                // byte[] result = contract.evaluateTransaction("GetElectionStatus", electionId);
                // String statusString = new String(result).trim();
                // return ElectionStatus.fromCode(statusString.toLowerCase());
                
                // MOCK: Por defecto devolvemos ACTIVE
                return ElectionStatus.ACTIVE;
                
            } catch (Exception e) {
                logger.error("Failed to query election status: {}", e.getMessage());
                return ElectionStatus.DRAFT;
            }
        }, "queryElectionStatus");
    }

    /**
     * Verifica que un voto fue registrado correctamente
     * 
     * @param transactionId ID de transacción del voto
     * @return true si el voto está en el ledger
     */
    public boolean verifyVoteTransaction(String transactionId) {
        logger.debug("Verifying vote transaction: {}", transactionId);
        
        if (!isConnected()) {
            logger.warn("No blockchain connection available, using mock verification");
            // Mock verification: verificamos que el ID tenga formato válido
            boolean isValid = transactionId != null && 
                            (transactionId.startsWith("TX-") || transactionId.startsWith("MOCK-TX-"));
            logger.debug("MOCK: Transaction {} verification result: {}", transactionId, isValid);
            return isValid;
        }

        return executeWithRetry(() -> {
            try {
                // MOCK: Simulación de verificación de transacción
                logger.debug("MOCK: Transaction verification simulated for: {}", transactionId);
                
                // En implementación real:
                // byte[] result = contract.evaluateTransaction("VerifyTransaction", transactionId);
                // String verification = new String(result).trim().toLowerCase();
                // boolean isValid = "true".equals(verification) || "valid".equals(verification);
                
                // Para el mock, verificamos que el ID tenga el formato esperado
                boolean isValid = transactionId != null && 
                                (transactionId.startsWith("TX-") || transactionId.startsWith("MOCK-TX-"));
                
                logger.debug("MOCK: Transaction {} verification result: {}", transactionId, isValid);
                return isValid;
                
            } catch (Exception e) {
                logger.error("Failed to verify transaction: {}", e.getMessage());
                return false;
            }
        }, "verifyVoteTransaction");
    }

    /**
     * Verifica si hay conexión activa con la blockchain
     * 
     * @return true si está conectado (en modo mock siempre false para usar fallbacks)
     */
    public boolean isConnected() {
        // En modo mock, devolvemos false para activar los fallbacks
        // En producción: return gateway != null && contract != null;
        if (mockMode) {
            return false; // Esto activa los métodos mock/fallback
        }
        return gateway != null && contract != null;
    }

    /**
     * Cierra la conexión con el Gateway
     */
    public void closeGateway() {
        if (gateway != null) {
            try {
                logger.info("Closing Fabric Gateway connection");
                gateway.close();
                gateway = null;
                contract = null;
            } catch (Exception e) {
                logger.error("Error closing gateway: {}", e.getMessage());
            }
        }
    }

    /**
     * Ejecuta una operación con reintentos y backoff exponencial
     * 
     * @param operation   Operación a ejecutar
     * @param operationName Nombre para logging
     * @return Resultado de la operación
     */
    private <T> T executeWithRetry(java.util.function.Supplier<T> operation, String operationName) {
        int maxAttempts = fabricProperties.getRetry().getMaxAttempts();
        long backoffDelayMs = fabricProperties.getRetry().getBackoffDelayMs();
        
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                return operation.get();
            } catch (Exception e) {
                if (attempt == maxAttempts) {
                    logger.error("Operation {} failed after {} attempts: {}", operationName, maxAttempts, e.getMessage());
                    throw new RuntimeException("Operation failed after retries", e);
                }
                
                logger.warn("Attempt {}/{} for {} failed, retrying in {}ms: {}", 
                    attempt, maxAttempts, operationName, backoffDelayMs, e.getMessage());
                
                try {
                    Thread.sleep(backoffDelayMs);
                    backoffDelayMs *= 2; // Backoff exponencial
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException("Operation interrupted", ie);
                }
            }
        }
        
        throw new RuntimeException("Should not reach here");
    }

    /**
     * Crea resultado mock para elección cerrada (fallback cuando no hay conexión)
     */
    private Map<String, Object> createMockElectionResult(String electionId) {
        Map<String, Object> result = new HashMap<>();
        result.put("electionId", electionId);
        result.put("status", "CLOSED");
        result.put("timestamp", System.currentTimeMillis());
        result.put("mockMode", true);
        return result;
    }

    /**
     * Crea resultados mock de votación (fallback cuando no hay conexión)
     */
    private Map<String, Object> createMockVoteResults(String electionId) {
        Map<String, Object> result = new HashMap<>();
        result.put("electionId", electionId);
        result.put("totalVotes", 0);
        result.put("timestamp", System.currentTimeMillis());
        result.put("mockMode", true);
        return result;
    }
}