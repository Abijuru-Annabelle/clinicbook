package rw.clinicbook.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

public record BookRequest(
        @NotNull(message = "Doctor is required") Long doctorId,
        @NotNull(message = "Start time is required") @Future(message = "Start time must be in the future") LocalDateTime startTime,
        @Size(max = 255) String reason) { }
