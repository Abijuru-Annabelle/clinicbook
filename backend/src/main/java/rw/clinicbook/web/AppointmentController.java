package rw.clinicbook.web;

import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import rw.clinicbook.dto.AppointmentDto;
import rw.clinicbook.dto.BookRequest;
import rw.clinicbook.dto.RescheduleRequest;
import rw.clinicbook.dto.StatusRequest;
import rw.clinicbook.service.AppointmentService;

@RestController
@RequestMapping("/api/appointments")
public class AppointmentController {

    private final AppointmentService appointments;

    public AppointmentController(AppointmentService appointments) {
        this.appointments = appointments;
    }

    @PostMapping
    @PreAuthorize("hasRole('PATIENT')")
    @ResponseStatus(HttpStatus.CREATED)
    public AppointmentDto book(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody BookRequest req) {
        return appointments.book(CurrentUser.id(jwt), req);
    }

    @GetMapping("/me")
    @PreAuthorize("hasRole('PATIENT')")
    public List<AppointmentDto> mine(@AuthenticationPrincipal Jwt jwt) {
        return appointments.mine(CurrentUser.id(jwt));
    }

    @PatchMapping("/{id}/cancel")
    @PreAuthorize("hasRole('PATIENT')")
    public AppointmentDto cancel(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id) {
        return appointments.cancel(CurrentUser.id(jwt), id);
    }

    @PatchMapping("/{id}/reschedule")
    @PreAuthorize("hasRole('PATIENT')")
    public AppointmentDto reschedule(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id,
                                     @Valid @RequestBody RescheduleRequest req) {
        return appointments.reschedule(CurrentUser.id(jwt), id, req.startTime());
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('DOCTOR')")
    public AppointmentDto updateStatus(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id,
                                       @Valid @RequestBody StatusRequest req) {
        return appointments.updateStatus(CurrentUser.id(jwt), id, req);
    }
}
