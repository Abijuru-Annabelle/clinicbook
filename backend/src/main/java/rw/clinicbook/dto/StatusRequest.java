package rw.clinicbook.dto;

import jakarta.validation.constraints.NotNull;
import rw.clinicbook.domain.AppointmentStatus;

public record StatusRequest(@NotNull(message = "Status is required") AppointmentStatus status, String notes) { }
