package rw.clinicbook.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record DepartmentRequest(
        @NotBlank(message = "Department name is required") @Size(max = 100) String name,
        @Size(max = 255) String description) { }
