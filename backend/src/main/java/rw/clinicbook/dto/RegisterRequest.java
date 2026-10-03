package rw.clinicbook.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank(message = "Full name is required") @Size(max = 100) String fullName,
        @NotBlank(message = "Email is required") @Email(message = "Enter a valid email") @Size(max = 150) String email,
        @Pattern(regexp = "^$|^\\+?\\d{9,15}$", message = "Phone must be 9-15 digits") String phone,
        @NotBlank @Size(min = 8, max = 100, message = "Password must be at least 8 characters") String password) { }
