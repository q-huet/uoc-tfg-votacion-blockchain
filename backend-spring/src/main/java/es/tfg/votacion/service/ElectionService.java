package es.tfg.votacion.service;

import es.tfg.votacion.model.Election;
import es.tfg.votacion.model.ElectionOption;
import es.tfg.votacion.model.ElectionStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Servicio mock para gestión de elecciones
 * 
 * Implementa almacenamiento en memoria para desarrollo y testing.
 * En producción, esto se reemplazaría con una base de datos.
 * 
 * @author Enrique Huet Adrover
 * @version 1.0
 */
@Service
public class ElectionService {

    private static final Logger logger = LoggerFactory.getLogger(ElectionService.class);
    
    private final Map<String, Election> elections = new ConcurrentHashMap<>();
    private final Map<String, Set<String>> userVotes = new ConcurrentHashMap<>(); // electionId -> Set<userId>
    private final Map<String, Map<String, Integer>> voteResults = new ConcurrentHashMap<>(); // electionId -> optionId -> count

    @PostConstruct
    public void init() {
        logger.info("Initializing ElectionService with mock elections");
        createMockElections();
    }

    /**
     * Crea elecciones mock para testing
     */
    private void createMockElections() {
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
    }

    /**
     * Obtiene todas las elecciones
     */
    public List<Election> getAllElections() {
        return new ArrayList<>(elections.values());
    }

    /**
     * Obtiene solo elecciones activas
     */
    public List<Election> getActiveElections() {
        return elections.values().stream()
            .filter(e -> e.status() == ElectionStatus.ACTIVE)
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
    public void registerVote(String electionId, String userId, String optionId) {
        // Registrar que el usuario votó
        userVotes.computeIfAbsent(electionId, k -> ConcurrentHashMap.newKeySet())
            .add(userId);
        
        // Actualizar contador de votos
        voteResults.computeIfAbsent(electionId, k -> new ConcurrentHashMap<>())
            .merge(optionId, 1, Integer::sum);
        
        // Actualizar total de votos en la elección
        Election election = elections.get(electionId);
        if (election != null) {
            Election updated = election.withTotalVotes(election.totalVotes() + 1);
            elections.put(electionId, updated);
        }
        
        logger.info("Vote registered: electionId={}, userId={}, optionId={}", 
            electionId, userId, optionId);
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
    public Election createElection(Election election) {
        elections.put(election.id(), election);
        logger.info("Election created: {}", election.id());
        return election;
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
        return updated;
    }

    /**
     * Cierra una elección
     */
    public Election closeElection(String electionId) {
        return updateElectionStatus(electionId, ElectionStatus.CLOSED);
    }
}
