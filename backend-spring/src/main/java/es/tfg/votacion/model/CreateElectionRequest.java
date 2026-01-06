package es.tfg.votacion.model;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.List;

public record CreateElectionRequest(
    @NotBlank(message = "Title is required")
    @Size(min = 5, max = 100, message = "Title must be between 5 and 100 characters")
    String title,

    @NotBlank(message = "Description is required")
    @Size(min = 10, max = 500, message = "Description must be between 10 and 500 characters")
    String description,

    @NotNull(message = "Options are required")
    @Size(min = 2, max = 10, message = "Must have between 2 and 10 options")
    @Valid
    List<ElectionOption> options,

    @NotNull(message = "Start time is required")
    // Removed @Future to allow starting elections "now" (which might be slightly in the past due to latency)
    LocalDateTime startTime,

    @NotNull(message = "End time is required")
    @Future(message = "End time must be in the future")
    LocalDateTime endTime
) {}
