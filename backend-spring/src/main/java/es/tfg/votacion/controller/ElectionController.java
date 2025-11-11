package es.tfg.votacion.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import es.tfg.votacion.dto.*;
import es.tfg.votacion.model.Election;
import es.tfg.votacion.model.ElectionStatus;
import es.tfg.votacion.model.Receipt;
import es.tfg.votacion.model.User;
import es.tfg.votacion.service.AuthService;
import es.tfg.votacion.service.ElectionService;
import es.tfg.votacion.service.FabricService;
import es.tfg.votacion.service.StorageService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.MessageDigest;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Controlador para gestión de elecciones y votaciones
 * 
 * Endpoints:
 * - GET /api/v1/elections - Lista elecciones activas
 * - GET /api/v1/elections/{id} - Detalle de elección
 * - POST /api/v1/elections/{id}/vote - Emitir voto
 * - POST /api/v1/elections/{id}/close - Cerrar votación (ADMIN)
 * - GET /api/v1/elections/{id}/results - Obtener resultados (ADMIN/AUDITOR)
 * - POST /api/v1/elections - Crear elección (ADMIN)
 * 
 * @author Enrique Huet Adrover
 * @version 1.0
 * @since Java 21
 */
@RestController
@RequestMapping("/api/v1/elections")
@CrossOrigin(origins = { "http://localhost:4200", "http://127.0.0.1:4200" })
public class ElectionController {

    private static final Logger logger = LoggerFactory.getLogger(ElectionController.class);
    
    private final AuthService authService;
    private final ElectionService electionService;
    private final FabricService fabricService;
    private final StorageService storageService;
    private final ObjectMapper objectMapper;

    public ElectionController(
            AuthService authService,
            ElectionService electionService,
            FabricService fabricService,
            StorageService storageService) {
        this.authService = authService;
        this.electionService = electionService;
        this.fabricService = fabricService;
        this.storageService = storageService;
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Lista todas las elecciones activas
     * GET /api/v1/elections
     * 
     * @param authHeader Authorization header con JWT
     * @param request HTTP request para logging
     * @return Lista de elecciones disponibles
     */
    @GetMapping
    public ResponseEntity<?> getActiveElections(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            HttpServletRequest request) {
        
        logger.info("Fetching active elections from IP: {}", request.getRemoteAddr());
        
        // Validar autenticación (opcional para ver lista)
        final User currentUser;
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            currentUser = authService.validateJwtToken(token);
        } else {
            currentUser = null;
        }
        
        try {
            List<Election> elections = electionService.getActiveElections();
            
            List<ElectionResponse> response = elections.stream()
                .map(election -> {
                    boolean hasVoted = currentUser != null && 
                        electionService.hasUserVoted(election.id(), currentUser.id());
                    return ElectionResponse.fromElection(
                        election, 
                        hasVoted, 
                        election.totalVotes()
                    );
                })
                .toList();
            
            logger.info("Returning {} active elections", response.size());
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Error fetching elections", e);
            return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse(
                    500,
                    "Internal Server Error",
                    "Error fetching elections",
                    request.getRequestURI()
                ));
        }
    }

    /**
     * Obtiene detalle de una elección específica
     * GET /api/v1/elections/{id}
     * 
     * @param electionId ID de la elección
     * @param authHeader Authorization header con JWT
     * @param request HTTP request
     * @return Detalle de la elección
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getElectionById(
            @PathVariable("id") String electionId,
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            HttpServletRequest request) {
        
        logger.info("Fetching election: {}", electionId);
        
        User user = null;
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            user = authService.validateJwtToken(token);
        }
        
        Optional<Election> electionOpt = electionService.getElectionById(electionId);
        
        if (electionOpt.isEmpty()) {
            return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponse(
                    404,
                    "Not Found",
                    "Election not found: " + electionId,
                    request.getRequestURI()
                ));
        }
        
        Election election = electionOpt.get();
        boolean hasVoted = user != null && 
            electionService.hasUserVoted(election.id(), user.id());
        
        ElectionResponse response = ElectionResponse.fromElection(
            election, 
            hasVoted, 
            election.totalVotes()
        );
        
        return ResponseEntity.ok(response);
    }

    /**
     * Emitir voto en una elección
     * POST /api/v1/elections/{id}/vote
     * 
     * @param electionId ID de la elección
     * @param voteRequest Datos del voto
     * @param authHeader Authorization header con JWT
     * @param request HTTP request
     * @return Recibo del voto (commitment + txId)
     */
    @PostMapping("/{id}/vote")
    public ResponseEntity<?> emitVote(
            @PathVariable("id") String electionId,
            @Valid @RequestBody VoteSubmissionRequest voteRequest,
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            HttpServletRequest request) {
        
        logger.info("Vote submission for election: {}", electionId);
        
        // Validar autenticación
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(new ErrorResponse(
                    401,
                    "Unauthorized",
                    "Authentication required",
                    request.getRequestURI()
                ));
        }
        
        String token = authHeader.substring(7);
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
        
        // Validar que la elección existe
        Optional<Election> electionOpt = electionService.getElectionById(electionId);
        if (electionOpt.isEmpty()) {
            return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponse(
                    404,
                    "Not Found",
                    "Election not found: " + electionId,
                    request.getRequestURI()
                ));
        }
        
        Election election = electionOpt.get();
        
        // Validar que la elección está activa
        if (!election.isActiveForVoting()) {
            return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse(
                    400,
                    "Bad Request",
                    "Election is not active for voting",
                    request.getRequestURI()
                ));
        }
        
        // Validar que el usuario no ha votado ya
        if (electionService.hasUserVoted(electionId, user.id())) {
            return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(new ErrorResponse(
                    409,
                    "Conflict",
                    "User has already voted in this election",
                    request.getRequestURI()
                ));
        }
        
        // Validar que la opción existe
        if (election.getOptionById(voteRequest.optionId()) == null) {
            return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse(
                    400,
                    "Bad Request",
                    "Invalid option ID: " + voteRequest.optionId(),
                    request.getRequestURI()
                ));
        }
        
        try {
            // Serializar voto a JSON
            Map<String, Object> voteData = Map.of(
                "electionId", electionId,
                "optionId", voteRequest.optionId(),
                "userId", user.id(),
                "timestamp", Instant.now().toString(),
                "comment", voteRequest.comment() != null ? voteRequest.comment() : ""
            );
            
            byte[] voteBytes = objectMapper.writeValueAsBytes(voteData);
            
            // Cifrar y almacenar voto
            String blobId = storageService.storeEncrypted(electionId, voteBytes);
            logger.info("Vote encrypted and stored: blobId={}", blobId);
            
            // Generar commitment hash
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(voteBytes);
            String commitment = Base64.getEncoder().encodeToString(hash);
            
            // Registrar en blockchain
            String transactionId = fabricService.emitVote(commitment, electionId);
            logger.info("Vote registered in blockchain: txId={}", transactionId);
            
            // Registrar voto en el servicio de elecciones
            electionService.registerVote(electionId, user.id(), voteRequest.optionId());
            
            // Crear recibo
            String verificationCode = generateVerificationCode();
            Receipt receipt = Receipt.of(electionId, user.id(), transactionId, verificationCode);
            
            // Crear response
            VoteSubmissionResponse response = new VoteSubmissionResponse(
                transactionId,
                electionId,
                Instant.now(),
                commitment,
                blobId,
                true,
                "Vote submitted successfully"
            );
            
            logger.info("Vote processed successfully for user: {} in election: {}", 
                user.username(), electionId);
            
            return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
            
        } catch (JsonProcessingException e) {
            logger.error("Error serializing vote data", e);
            return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse(
                    500,
                    "Internal Server Error",
                    "Error processing vote data",
                    request.getRequestURI()
                ));
        } catch (Exception e) {
            logger.error("Error submitting vote", e);
            return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse(
                    500,
                    "Internal Server Error",
                    "Error submitting vote: " + e.getMessage(),
                    request.getRequestURI()
                ));
        }
    }

    /**
     * Cerrar una elección (solo ADMIN)
     * POST /api/v1/elections/{id}/close
     * 
     * @param electionId ID de la elección a cerrar
     * @param authHeader Authorization header con JWT
     * @param request HTTP request
     * @return Resumen del cierre
     */
    @PostMapping("/{id}/close")
    public ResponseEntity<?> closeElection(
            @PathVariable("id") String electionId,
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            HttpServletRequest request) {
        
        logger.info("Closing election: {}", electionId);
        
        // Validar autenticación y permisos ADMIN
        User user = validateAdminAccess(authHeader, request);
        if (user == null) {
            return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(new ErrorResponse(
                    403,
                    "Forbidden",
                    "Admin access required",
                    request.getRequestURI()
                ));
        }
        
        Optional<Election> electionOpt = electionService.getElectionById(electionId);
        if (electionOpt.isEmpty()) {
            return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponse(
                    404,
                    "Not Found",
                    "Election not found: " + electionId,
                    request.getRequestURI()
                ));
        }
        
        try {
            Election closed = electionService.closeElection(electionId);
            
            Map<String, Object> response = Map.of(
                "electionId", electionId,
                "status", closed.status(),
                "totalVotes", closed.totalVotes(),
                "closedAt", LocalDateTime.now(),
                "closedBy", user.username()
            );
            
            logger.info("Election closed: {} by {}", electionId, user.username());
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Error closing election", e);
            return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse(
                    500,
                    "Internal Server Error",
                    "Error closing election",
                    request.getRequestURI()
                ));
        }
    }

    /**
     * Obtener resultados de una elección (ADMIN/AUDITOR)
     * GET /api/v1/elections/{id}/results
     * 
     * @param electionId ID de la elección
     * @param authHeader Authorization header con JWT
     * @param request HTTP request
     * @return Resultados de la votación
     */
    @GetMapping("/{id}/results")
    public ResponseEntity<?> getElectionResults(
            @PathVariable("id") String electionId,
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            HttpServletRequest request) {
        
        logger.info("Fetching results for election: {}", electionId);
        
        // Validar autenticación y permisos
        User user = validateAdminOrAuditorAccess(authHeader, request);
        if (user == null) {
            return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(new ErrorResponse(
                    403,
                    "Forbidden",
                    "Admin or Auditor access required",
                    request.getRequestURI()
                ));
        }
        
        Optional<Election> electionOpt = electionService.getElectionById(electionId);
        if (electionOpt.isEmpty()) {
            return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponse(
                    404,
                    "Not Found",
                    "Election not found: " + electionId,
                    request.getRequestURI()
                ));
        }
        
        Election election = electionOpt.get();
        
        // Solo mostrar resultados si está cerrada
        if (election.status() != ElectionStatus.CLOSED && 
            election.status() != ElectionStatus.COMPLETED) {
            return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse(
                    400,
                    "Bad Request",
                    "Election must be closed to view results",
                    request.getRequestURI()
                ));
        }
        
        Map<String, Integer> results = electionService.getElectionResults(electionId);
        int totalVotes = election.totalVotes();
        
        List<ElectionResultsResponse.OptionResult> optionResults = election.options().stream()
            .map(option -> {
                int votes = results.getOrDefault(option.optionId(), 0);
                double percentage = totalVotes > 0 ? (votes * 100.0 / totalVotes) : 0.0;
                return new ElectionResultsResponse.OptionResult(
                    option.optionId(),
                    option.title(),
                    votes,
                    percentage
                );
            })
            .toList();
        
        ElectionResultsResponse response = new ElectionResultsResponse(
            electionId,
            election.title(),
            election.status(),
            Instant.now(),
            totalVotes,
            optionResults,
            "blockchain-audit-trail-" + electionId
        );
        
        logger.info("Results fetched for election: {} by {}", electionId, user.username());
        return ResponseEntity.ok(response);
    }

    /**
     * Valida acceso ADMIN
     */
    private User validateAdminAccess(String authHeader, HttpServletRequest request) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return null;
        }
        
        String token = authHeader.substring(7);
        User user = authService.validateJwtToken(token);
        
        if (user == null || !authService.hasRole(user, es.tfg.votacion.model.UserRole.ADMIN)) {
            return null;
        }
        
        return user;
    }

    /**
     * Valida acceso ADMIN o AUDITOR
     */
    private User validateAdminOrAuditorAccess(String authHeader, HttpServletRequest request) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return null;
        }
        
        String token = authHeader.substring(7);
        User user = authService.validateJwtToken(token);
        
        if (user == null || !authService.hasAnyRole(user, 
                es.tfg.votacion.model.UserRole.ADMIN, 
                es.tfg.votacion.model.UserRole.AUDITOR)) {
            return null;
        }
        
        return user;
    }

    /**
     * Genera un código de verificación aleatorio
     */
    private String generateVerificationCode() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder code = new StringBuilder();
        Random random = new Random();
        
        for (int i = 0; i < 12; i++) {
            code.append(chars.charAt(random.nextInt(chars.length())));
        }
        
        return code.toString();
    }
}