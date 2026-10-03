package rw.clinicbook.web;

import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import rw.clinicbook.dto.AppointmentDto;
import rw.clinicbook.dto.AvailabilityDto;
import rw.clinicbook.dto.AvailabilityRequest;
import rw.clinicbook.service.AppointmentService;
import rw.clinicbook.service.AvailabilityService;

/** Endpoints for the logged-in doctor ("my schedule", "my availability"). */
@RestController
@RequestMapping("/api/doctor")
@PreAuthorize("hasRole('DOCTOR')")
public class DoctorSelfController {

    private final AppointmentService appointments;
    private final AvailabilityService availability;

    public DoctorSelfController(AppointmentService appointments, AvailabilityService availability) {
        this.appointments = appointments;
        this.availability = availability;
    }

    @GetMapping("/appointments")
    public List<AppointmentDto> schedule(@AuthenticationPrincipal Jwt jwt,
                                         @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return appointments.doctorDay(CurrentUser.id(jwt), date);
    }

    @GetMapping("/availability")
    public List<AvailabilityDto> myAvailability(@AuthenticationPrincipal Jwt jwt) {
        return availability.mine(CurrentUser.id(jwt));
    }

    @PostMapping("/availability")
    @ResponseStatus(HttpStatus.CREATED)
    public AvailabilityDto add(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody AvailabilityRequest req) {
        return availability.add(CurrentUser.id(jwt), req);
    }

    @DeleteMapping("/availability/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void remove(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id) {
        availability.delete(CurrentUser.id(jwt), id);
    }
}
