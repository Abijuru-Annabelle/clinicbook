package rw.clinicbook.web;

import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import rw.clinicbook.dto.DoctorDto;
import rw.clinicbook.dto.DoctorRequest;
import rw.clinicbook.dto.SlotDto;
import rw.clinicbook.service.DoctorService;

@RestController
@RequestMapping("/api/doctors")
public class DoctorController {

    private final DoctorService doctors;

    public DoctorController(DoctorService doctors) {
        this.doctors = doctors;
    }

    @GetMapping
    public List<DoctorDto> search(@RequestParam(required = false) String search,
                                  @RequestParam(required = false) Long departmentId) {
        return doctors.search(search, departmentId);
    }

    @GetMapping("/{id}")
    public DoctorDto get(@PathVariable Long id) {
        return doctors.get(id);
    }

    @GetMapping("/{id}/slots")
    public List<SlotDto> slots(@PathVariable Long id,
                               @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return doctors.slots(id, date);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.CREATED)
    public DoctorDto create(@Valid @RequestBody DoctorRequest req) {
        return doctors.create(req);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        doctors.delete(id);
    }
}
