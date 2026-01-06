package es.tfg.votacion.dto;

import es.tfg.votacion.model.ElectionStatus;
import java.time.Instant;
import java.util.List;

/**
 * DTO para response de resultados de elección
 * 
 * @author Enrique Huet Adrover
 */
public record ElectionResultsResponse(
    String electionId,
    String title,
    ElectionStatus status,
    Instant closedAt,
    int totalVotes,
    List<OptionResult> results,
    String auditTrail
) {
    public record OptionResult(
        String optionId,
        String label,
        int votes,
        double percentage
    ) {}
}
