package rw.clinicbook.dto;

import java.time.Instant;

public record MedicalDocumentDto(String id, Long appointmentId, String fileName, String contentType, long size,
                                 Long uploadedBy, String uploaderName, Instant uploadedAt) { }
