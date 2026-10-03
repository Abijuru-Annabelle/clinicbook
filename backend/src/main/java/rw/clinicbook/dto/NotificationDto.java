package rw.clinicbook.dto;

import java.time.Instant;

public record NotificationDto(String id, String channel, String type, String message, String status, Instant createdAt) { }
