package rw.clinicbook.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record DoctorRequest(
        @NotBlank(message = "Full name is required") @Size(max = 100) String fullName,
        @NotBlank(message = "Email is required") @Email(message = "Enter a valid email") String email,
        String phone,
        @NotBlank(message = "Specialization is required") @Size(max = 100) String specialization,
        @NotBlank(message = "License number is required") @Size(max = 50) String licenseNumber,
        @NotNull(message = "Department is required") Long departmentId,
        @Min(10) @Max(120) Integer slotMinutes) { }
