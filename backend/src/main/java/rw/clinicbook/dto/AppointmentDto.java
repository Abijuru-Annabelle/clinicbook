package rw.clinicbook.dto;

import java.time.LocalDateTime;

public record AppointmentDto(Long id, Long doctorId, String doctorName, String departmentName,
                             Long patientId, String patientName,
                             LocalDateTime startTime, LocalDateTime endTime,
                             String status, String reason, String notes) { }
