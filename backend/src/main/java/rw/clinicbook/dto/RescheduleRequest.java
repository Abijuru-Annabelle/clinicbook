package rw.clinicbook.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

public record RescheduleRequest(
        @NotNull(message = "Start time is required") @Future(message = "Start time must be in the future") LocalDateTime startTime) { }
