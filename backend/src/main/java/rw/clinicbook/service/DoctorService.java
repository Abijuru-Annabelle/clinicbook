package rw.clinicbook.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rw.clinicbook.domain.*;
import rw.clinicbook.dto.DoctorDto;
import rw.clinicbook.dto.DoctorRequest;
import rw.clinicbook.dto.Mapper;
import rw.clinicbook.dto.SlotDto;
import rw.clinicbook.exception.ApiException;
import rw.clinicbook.repository.*;

@Service
public class DoctorService {

    private static final Logger log = LoggerFactory.getLogger(DoctorService.class);

    private final DoctorRepository doctors;
    private final DepartmentRepository departments;
    private final UserRepository users;
    private final AvailabilityRepository availability;
    private final AppointmentRepository appointments;
    private final PasswordEncoder encoder;
    private final String defaultPassword;

    public DoctorService(DoctorRepository doctors, DepartmentRepository departments, UserRepository users,
                         AvailabilityRepository availability, AppointmentRepository appointments,
                         PasswordEncoder encoder, @Value("${app.default-doctor-password}") String defaultPassword) {
        this.doctors = doctors;
        this.departments = departments;
        this.users = users;
        this.availability = availability;
        this.appointments = appointments;
        this.encoder = encoder;
        this.defaultPassword = defaultPassword;
    }

    @Transactional(readOnly = true)
    public List<DoctorDto> search(String search, Long departmentId) {
        String q = search == null ? "" : search.trim().toLowerCase(Locale.ROOT);
        return doctors.findAllActiveWithDetails().stream()
                .filter(d -> departmentId == null || d.getDepartment().getId().equals(departmentId))
                .filter(d -> q.isEmpty()
                        || d.getUser().getFullName().toLowerCase(Locale.ROOT).contains(q)
                        || d.getSpecialization().toLowerCase(Locale.ROOT).contains(q))
                .map(Mapper::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public DoctorDto get(Long id) {
        return Mapper.toDto(find(id));
    }

    Doctor find(Long id) {
        Doctor d = doctors.findById(id).orElseThrow(() -> ApiException.notFound("Doctor not found"));
        if (!d.getUser().isEnabled()) throw ApiException.notFound("Doctor not found");
        return d;
    }

    /** Admin creates a doctor + their login account (role DOCTOR). */
    @Transactional
    public DoctorDto create(DoctorRequest req) {
        String email = req.email().trim().toLowerCase(Locale.ROOT);
        if (users.existsByEmailIgnoreCase(email)) throw ApiException.conflict("Email already in use");
        if (doctors.existsByLicenseNumberIgnoreCase(req.licenseNumber().trim()))
            throw ApiException.conflict("License number already registered");
        Department dept = departments.findById(req.departmentId())
                .orElseThrow(() -> ApiException.badRequest("Department not found"));

        User user = new User();
        user.setFullName(req.fullName().trim());
        user.setEmail(email);
        user.setPhone(req.phone() == null || req.phone().isBlank() ? null : req.phone().trim());
        user.setPasswordHash(encoder.encode(defaultPassword));
        user.setRole(Role.DOCTOR);
        users.save(user);

        Doctor doctor = new Doctor();
        doctor.setUser(user);
        doctor.setDepartment(dept);
        doctor.setSpecialization(req.specialization().trim());
        doctor.setLicenseNumber(req.licenseNumber().trim());
        doctor.setSlotMinutes(req.slotMinutes() == null ? 30 : req.slotMinutes());
        doctors.save(doctor);

        log.info("Doctor account created for {}", email);
        return Mapper.toDto(doctor);
    }

    /** Soft delete: keeps appointment history, disables the login. */
    @Transactional
    public void delete(Long id) {
        Doctor d = find(id);
        if (appointments.existsByDoctorIdAndStatus(id, AppointmentStatus.BOOKED))
            throw ApiException.conflict("Doctor has upcoming appointments. Cancel or complete them first.");
        d.getUser().setEnabled(false);
    }

    /** Builds the day's time slots from the doctor's weekly availability, marking booked/past ones unavailable. */
    @Transactional(readOnly = true)
    public List<SlotDto> slots(Long doctorId, LocalDate date) {
        Doctor doctor = find(doctorId);
        int minutes = doctor.getSlotMinutes();
        int dayOfWeek = date.getDayOfWeek().getValue();

        Set<LocalDateTime> taken = appointments
                .findForDoctorBetween(doctorId, date.atStartOfDay(), date.plusDays(1).atStartOfDay()).stream()
                .filter(a -> a.getStatus() != AppointmentStatus.CANCELLED)
                .map(Appointment::getStartTime)
                .collect(Collectors.toSet());

        LocalDateTime now = LocalDateTime.now();
        List<SlotDto> result = new ArrayList<>();
        for (Availability window : availability.findByDoctorIdAndDayOfWeekOrderByStartTimeAsc(doctorId, dayOfWeek)) {
            LocalDateTime t = date.atTime(window.getStartTime());
            LocalDateTime end = date.atTime(window.getEndTime());
            while (!t.plusMinutes(minutes).isAfter(end)) {
                result.add(new SlotDto(t, !taken.contains(t) && t.isAfter(now)));
                t = t.plusMinutes(minutes);
            }
        }
        return result;
    }
}
