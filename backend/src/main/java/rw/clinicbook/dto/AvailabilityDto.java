package rw.clinicbook.dto;

import java.time.LocalTime;

public record AvailabilityDto(Long id, Integer dayOfWeek, LocalTime startTime, LocalTime endTime) { }
