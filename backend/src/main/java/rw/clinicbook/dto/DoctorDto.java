package rw.clinicbook.dto;

public record DoctorDto(Long id, String fullName, String email, String specialization, String licenseNumber,
                        Long departmentId, String departmentName, Integer slotMinutes) { }
