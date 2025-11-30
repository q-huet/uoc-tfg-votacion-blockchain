package es.tfg.votacion.dto;

import es.tfg.votacion.model.Election;
import es.tfg.votacion.model.ElectionStatus;
import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO para response de información de elección
 * 
 * @author Enrique Huet Adrover
 */
public record ElectionResponse(
    String id,
    String title,
    String description,
    ElectionStatus status,
    LocalDateTime startTime,
    LocalDateTime endTime,
    List<OptionInfo> options,
    boolean hasVoted,
    int totalVotes
) {
    public static ElectionResponse fromElection(Election election, boolean hasVoted, int totalVotes) {
        List<OptionInfo> options = election.options().stream()
            .map(opt -> new OptionInfo(opt.optionId(), opt.title(), opt.description()))
            .toList();
            
        return new ElectionResponse(
            election.id(),
            election.title(),
            election.description(),
            election.status(),
            election.startTime(),
            election.endTime(),
            options,
            hasVoted,
            totalVotes
        );
    }
    
    public record OptionInfo(
        String optionId,
        String title,
        String description
    ) {}
}
