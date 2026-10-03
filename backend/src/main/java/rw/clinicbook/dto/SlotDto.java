package rw.clinicbook.dto;

import java.time.LocalDateTime;

public record SlotDto(LocalDateTime startTime, boolean available) { }
