package rw.clinicbook.dto;

public record UserDto(Long id, String fullName, String email, String role, Long patientId, Long doctorId) { }
