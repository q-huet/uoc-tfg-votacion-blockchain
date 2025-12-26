package es.tfg.votacion.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import es.tfg.votacion.model.Election;
import es.tfg.votacion.model.ElectionOption;
import es.tfg.votacion.model.ElectionStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Servicio mock para gestión de elecciones
 * 
 * Implementa almacenamiento en memoria con persistencia en archivo JSON para desarrollo.
 * 
 * @author Enrique Huet Adrover
 * @version 1.1
 */
@Service
public class ElectionService {

    private static final Logger logger = LoggerFactory.getLogger(ElectionService.class);
    private static final String DATA_FILE = "data/elections-db.json";
    
    private final Map<String, Election> elections = new ConcurrentHashMap<>();
    private final Map<String, Set<String>> userVotes = new ConcurrentHashMap<>(); // electionId -> Set<userId>
    private final Map<String, Map<String, Integer>> voteResults = new ConcurrentHashMap<>(); // electionId -> optionId -> count
    private final Map<String, String> blobTransactions = new ConcurrentHashMap<>(); // blobId -> transactionId
    
    @org.springframework.beans.factory.annotation.Autowired
    private StorageService storageService;

    @org.springframework.beans.factory.annotation.Autowired
    private FabricService fabricService;

    @org.springframework.beans.factory.annotation.Autowired
    private CryptoService cryptoService;

    private final ObjectMapper objectMapper;

    public ElectionService() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
        this.objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        this.objectMapper.configure(com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    @PostConstruct
    public void init() {
        logger.info("Initializing ElectionService");
        loadData();
    }

    private void loadData() {
        File file = new File(DATA_FILE);
        if (file.exists()) {
            try {
                PersistedData data = objectMapper.readValue(file, PersistedData.class);
                
                if (data.elections != null) {
                    elections.putAll(data.elections);
                }
                if (data.userVotes != null) {
                    userVotes.putAll(data.userVotes);
                }
                if (data.voteResults != null) {
                    voteResults.putAll(data.voteResults);
                }
                if (data.blobTransactions != null) {
                    blobTransactions.putAll(data.blobTransactions);
                }
                logger.info("Loaded {} elections from disk", elections.size());
                return;
            } catch (IOException e) {
                logger.error("Error loading data from disk", e);
            }
        }
        
        logger.info("No existing data found, creating mock elections");
        createMockElections();
        saveData();
    }

    private void saveData() {
        try {
            File file = new File(DATA_FILE);
            File parent = file.getParentFile();
            if (!parent.exists() && !parent.mkdirs()) {
                logger.error("Could not create data directory");
                return;
            }
            
            PersistedData data = new PersistedData();
            data.elections = new HashMap<>(elections);
            data.userVotes = new HashMap<>(userVotes);
            data.voteResults = new HashMap<>(voteResults);
            data.blobTransactions = new HashMap<>(blobTransactions);
            
            objectMapper.writeValue(file, data);
            logger.info("Data saved to disk");
        } catch (IOException e) {
            logger.error("Error saving data to disk", e);
        }
    }

    private static class PersistedData {
        public Map<String, Election> elections;
        public Map<String, Set<String>> userVotes;
        public Map<String, Map<String, Integer>> voteResults;
        public Map<String, String> blobTransactions;
    }

    /**
     * Crea elecciones mock para testing
     */
    private void createMockElections() {
        logger.info("Mock elections creation disabled.");
        /*
        // Elección 1: Activa
        Election election1 = new Election(
            "election-001",
            "Delegado Sindical 2025",
            "Elección del delegado sindical para el año 2025. Se elegirá un representante de los trabajadores.",
            List.of(
                new ElectionOption("opt1", "Juan García Martínez", "Candidato con 10 años de experiencia", 1),
                new ElectionOption("opt2", "María López Sánchez", "Candidata del turno de tarde", 2),
                new ElectionOption("opt3", "Pedro Rodríguez Pérez", "Candidato independiente", 3)
            ),
            ElectionStatus.ACTIVE,
            LocalDateTime.now().minusHours(1),
            LocalDateTime.now().plusDays(7),
            "admin",
            LocalDateTime.now().minusDays(10),
            0,
            1,
            false,
            true
        );
        
        // Elección 2: Cerrada
        Election election2 = new Election(
            "election-002",
            "Horario de Verano 2024",
            "Votación para decidir el horario de trabajo durante el verano.",
            List.of(
                new ElectionOption("opt1", "Horario intensivo (7:00-15:00)", "Jornada continua de mañana", 1),
                new ElectionOption("opt2", "Horario partido (9:00-14:00 y 16:00-19:00)", "Jornada partida tradicional", 2)
            ),
            ElectionStatus.CLOSED,
            LocalDateTime.now().minusDays(30),
            LocalDateTime.now().minusDays(23),
            "admin",
            LocalDateTime.now().minusDays(40),
            45,
            1,
            false,
            true
        );
        
        // Elección 3: Borrador
        Election election3 = new Election(
            "election-003",
            "Mejoras en Comedor 2025",
            "Propuestas de mejora para el comedor de empresa.",
            List.of(
                new ElectionOption("opt1", "Ampliar menú vegetariano", "Más opciones sin carne", 1),
                new ElectionOption("opt2", "Extender horario", "Abrir más temprano y cerrar más tarde", 2),
                new ElectionOption("opt3", "Mejorar infraestructura", "Renovar mesas y sillas", 3),
                new ElectionOption("opt4", "Todas las anteriores", "Implementar todas las mejoras", 4)
            ),
            ElectionStatus.DRAFT,
            LocalDateTime.now().plusDays(15),
            LocalDateTime.now().plusDays(22),
            "pedro.lopez",
            LocalDateTime.now().minusDays(2),
            0,
            1,
            false,
            true
        );
        
        elections.put(election1.id(), election1);
        elections.put(election2.id(), election2);
        elections.put(election3.id(), election3);
        
        // Inicializar resultados para elección cerrada
        Map<String, Integer> results2 = new HashMap<>();
        results2.put("opt1", 28);
        results2.put("opt2", 17);
        voteResults.put(election2.id(), results2);
        
        logger.info("Created {} mock elections", elections.size());
        */
    }

    /**
     * Obtiene todas las elecciones
     */
    public List<Election> getAllElections() {
        return new ArrayList<>(elections.values());
    }

    /**
     * Obtiene elecciones públicas (Activas, Cerradas, Completadas)
     */
    public List<Election> getActiveElections() {
        return elections.values().stream()
            .filter(e -> e.status() == ElectionStatus.ACTIVE || 
                         e.status() == ElectionStatus.CLOSED || 
                         e.status() == ElectionStatus.COMPLETED)
            .toList();
    }

    /**
     * Obtiene una elección por ID
     */
    public Optional<Election> getElectionById(String electionId) {
        return Optional.ofNullable(elections.get(electionId));
    }

    /**
     * Verifica si un usuario ya votó en una elección
     */
    public boolean hasUserVoted(String electionId, String userId) {
        Set<String> voters = userVotes.get(electionId);
        return voters != null && voters.contains(userId);
    }

    /**
     * Registra un voto
     */
    public void registerVote(String electionId, String userId, String optionId, String blobId, String transactionId) {
        // Registrar que el usuario votó
        userVotes.computeIfAbsent(electionId, k -> ConcurrentHashMap.newKeySet())
            .add(userId);
        
        // Actualizar contador de votos
        voteResults.computeIfAbsent(electionId, k -> new ConcurrentHashMap<>())
            .merge(optionId, 1, Integer::sum);
            
        // Registrar mapeo blob -> transaccion
        if (blobId != null && transactionId != null) {
            blobTransactions.put(blobId, transactionId);
        }
        
        // Actualizar total de votos en la elección
        Election election = elections.get(electionId);
        if (election != null) {
            Election updated = election.withTotalVotes(election.totalVotes() + 1);
            elections.put(electionId, updated);
        }
        
        logger.info("Vote registered: electionId={}, userId={}, optionId={}, txId={}", 
            electionId, userId, optionId, transactionId);
        saveData();
    }

    /**
     * Obtiene los resultados de una elección
     */
    public Map<String, Integer> getElectionResults(String electionId) {
        return voteResults.getOrDefault(electionId, new HashMap<>());
    }

    /**
     * Obtiene el número total de votantes en una elección
     */
    public int getTotalVoters(String electionId) {
        Set<String> voters = userVotes.get(electionId);
        return voters != null ? voters.size() : 0;
    }

    /**
     * Crea una nueva elección
     */
    public es.tfg.votacion.dto.ElectionCreationResult createElection(Election election) {
        // Generate Keys
        java.security.KeyPair keyPair = cryptoService.generateKeyPair();
        String publicKeyPem = cryptoService.publicKeyToPem(keyPair.getPublic());
        String privateKeyPem = cryptoService.privateKeyToPem(keyPair.getPrivate());

        // Update election with public key
        Election electionWithKey = new Election(
            election.id(), election.title(), election.description(), election.options(),
            election.status(), election.startTime(), election.endTime(),
            election.createdBy(), election.createdAt(), election.totalVotes(),
            election.maxVotesPerUser(), election.allowVoteModification(),
            election.requireAuditTrail(), publicKeyPem
        );

        elections.put(electionWithKey.id(), electionWithKey);
        logger.info("Election created: {}", electionWithKey.id());
        
        // Create on Blockchain
        try {
            if (fabricService != null) {
                fabricService.createElection(electionWithKey.id());
                logger.info("Election created on blockchain: {}", electionWithKey.id());
            }
        } catch (Exception e) {
            logger.error("Failed to create election on blockchain: {}", e.getMessage());
            // We might want to rollback or mark as failed, but for PoC we continue
        }
        
        saveData();
        return new es.tfg.votacion.dto.ElectionCreationResult(electionWithKey, privateKeyPem);
    }

    /**
     * Actualiza el estado de una elección
     */
    public Election updateElectionStatus(String electionId, ElectionStatus newStatus) {
        Election election = elections.get(electionId);
        if (election == null) {
            return null;
        }
        
        Election updated = election.withStatus(newStatus);
        elections.put(electionId, updated);
        logger.info("Election status updated: {} -> {}", electionId, newStatus);
        saveData();
        return updated;
    }

    /**
     * Cierra una elección y realiza el recuento de votos
     */
    public Election closeElection(String electionId, String privateKeyPem) {
        logger.info("Closing election process started: {}", electionId);

        // 1. Close on Blockchain
        try {
            if (fabricService != null) {
                fabricService.closeElection(electionId);
                logger.info("Election closed on blockchain: {}", electionId);
            }
        } catch (Exception e) {
            logger.error("Failed to close election on blockchain: {}", e.getMessage());
            // Continue to close locally
        }

        // 2. Perform Recount (Decryption)
        if (storageService != null) {
            logger.info("Starting vote recount (decryption) for election: {}", electionId);
            Map<String, Integer> recountedResults = new HashMap<>();
            List<String> blobs = storageService.listElectionBlobs(electionId);
            
            int decryptedCount = 0;
            for (String blobId : blobs) {
                try {
                    byte[] decrypted = storageService.loadDecrypted(blobId);
                    
                    // --- VERIFICATION START ---
                    try {
                        java.security.MessageDigest digest = java.security.MessageDigest.getInstance("SHA-256");
                        byte[] hash = digest.digest(decrypted);
                        String calculatedCommitment = java.util.Base64.getEncoder().encodeToString(hash);
                        
                        String transactionId = blobTransactions.get(blobId);
                        if (transactionId != null) {
                            String blockchainCommitment = fabricService.getVoteCommitment(transactionId);
                            if (blockchainCommitment != null) {
                                if (!calculatedCommitment.equals(blockchainCommitment)) {
                                    logger.error("INTEGRITY FAILURE: Blob {} commitment {} does not match blockchain commitment {}", 
                                        blobId, calculatedCommitment, blockchainCommitment);
                                    continue; // Skip this vote!
                                } else {
                                    logger.debug("Integrity check passed for blob {}", blobId);
                                }
                            } else {
                                 if (fabricService.isConnected()) {
                                     logger.error("Transaction {} not found in blockchain. Skipping vote.", transactionId);
                                     continue;
                                 } else {
                                     logger.warn("Could not verify vote against blockchain (not connected). Proceeding with local data.");
                                 }
                            }
                        } else {
                            logger.warn("No transaction ID found for blob {}. Skipping verification.", blobId);
                        }
                    } catch (Exception e) {
                        logger.error("Error during vote verification: {}", e.getMessage());
                    }
                    // --- VERIFICATION END ---

                    com.fasterxml.jackson.databind.JsonNode voteNode = objectMapper.readTree(decrypted);
                    
                    String optionId = null;
                    if (voteNode.has("encryptedVote")) {
                        // RSA Decrypt using the provided private key
                        String encryptedVoteBase64 = voteNode.get("encryptedVote").asText();
                        try {
                            optionId = cryptoService.decrypt(encryptedVoteBase64, privateKeyPem);
                        } catch (Exception e) {
                            logger.error("Failed to RSA decrypt vote {}: {}", blobId, e.getMessage());
                            continue; // Skip this vote if decryption fails
                        }
                    } else if (voteNode.has("optionId")) {
                        // Legacy/Dev fallback
                        optionId = voteNode.get("optionId").asText();
                    }
                    
                    if (optionId != null) {
                        recountedResults.merge(optionId, 1, Integer::sum);
                        decryptedCount++;
                    }
                } catch (Exception e) {
                    logger.error("Failed to process vote blob {}: {}", blobId, e.getMessage());
                }
            }
            
            logger.info("Recount finished. Decrypted {} votes.", decryptedCount);
            
            // Log discrepancy if any
            Map<String, Integer> currentResults = voteResults.getOrDefault(electionId, new HashMap<>());
            if (!currentResults.equals(recountedResults)) {
                logger.warn("DISCREPANCY DETECTED! Incremental count: {}, Decrypted count: {}", currentResults, recountedResults);
            } else {
                logger.info("Integrity check passed: Incremental count matches decrypted count.");
            }

            // 3. Update Results with Recounted values (Source of Truth)
            voteResults.put(electionId, recountedResults);
            
            // Update total votes based on recount
            int totalRecountedVotes = recountedResults.values().stream().mapToInt(Integer::intValue).sum();
            Election election = elections.get(electionId);
            if (election != null) {
                Election updated = election.withTotalVotes(totalRecountedVotes);
                elections.put(electionId, updated);
            }
        }

        return updateElectionStatus(electionId, ElectionStatus.CLOSED);
    }
}
