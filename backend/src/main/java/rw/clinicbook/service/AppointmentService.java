package rw.clinicbook.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rw.clinicbook.domain.Appointment;
import rw.clinicbook.domain.AppointmentStatus;
import rw.clinicbook.domain.Doctor;
import rw.clinicbook.domain.Patient;
import rw.clinicbook.dto.AppointmentDto;
import rw.clinicbook.dto.BookRequest;
import rw.clinicbook.dto.Mapper;
import rw.clinicbook.dto.StatusRequest;
import rw.clinicbook.exception.ApiException;
import rw.clinicbook.messaging.Notifier;
import rw.clinicbook.repository.AppointmentRepository;
import rw.clinicbook.repository.DoctorRepository;
import rw.clinicbook.repository.PatientRepository;

@Service
public class AppointmentService {

    static final int CHANGE_LIMIT_HOURS = 2;

    private final AppointmentRepository appointments;
    private final PatientRepository patients;
    private final DoctorRepository doctors;
    private final DoctorService doctorService;
    private final Notifier notifier;

    public AppointmentService(AppointmentRepository appointments, PatientRepository patients,
                              DoctorRepository doctors, DoctorService doctorService, Notifier notifier) {
        this.appointments = appointments;
        this.patients = patients;
        this.doctors = doctors;
        this.doctorService = doctorService;
        this.notifier = notifier;
    }

    // ---------- Patient actions ----------

    @Transactional
    public AppointmentDto book(Long userId, BookRequest req) {
        Patient patient = currentPatient(userId);
        Doctor doctor = doctorService.find(req.doctorId());
        requireFreeSlot(doctor, req.startTime());

        Appointment a = new Appointment();
        a.setPatient(patient);
        a.setDoctor(doctor);
        a.setStartTime(req.startTime());
        a.setEndTime(req.startTime().plusMinutes(doctor.getSlotMinutes()));
        a.setReason(req.reason() == null ? null : req.reason().trim());
        saveOrConflict(a);
        notifyBoth(a, "appointment.booked", "Appointment confirmed",
                "Your appointment with " + doctorName(a) + " (" + a.getDoctor().getDepartment().getName() + ") is confirmed for "
                        + Notifier.when(a.getStartTime()) + ". Reference #" + a.getId() + ".",
                "New appointment: " + patientName(a) + " on " + Notifier.when(a.getStartTime()) + ".");
        return Mapper.toDto(a);
    }

    @Transactional(readOnly = true)
    public List<AppointmentDto> mine(Long userId) {
        Patient patient = currentPatient(userId);
        return appointments.findForPatient(patient.getId()).stream().map(Mapper::toDto).toList();
    }

    @Transactional
    public AppointmentDto cancel(Long userId, Long id) {
        Appointment a = ownedByPatient(userId, id);
        requireChangeable(a, "cancelled");
        a.setStatus(AppointmentStatus.CANCELLED);
        notifyBoth(a, "appointment.cancelled", "Appointment cancelled",
                "Your appointment with " + doctorName(a) + " on " + Notifier.when(a.getStartTime()) + " has been cancelled.",
                patientName(a) + " cancelled the appointment on " + Notifier.when(a.getStartTime()) + ".");
        return Mapper.toDto(a);
    }

    @Transactional
    public AppointmentDto reschedule(Long userId, Long id, LocalDateTime newStart) {
        Appointment a = ownedByPatient(userId, id);
        requireChangeable(a, "rescheduled");
        requireFreeSlot(a.getDoctor(), newStart);
        a.setStartTime(newStart);
        a.setEndTime(newStart.plusMinutes(a.getDoctor().getSlotMinutes()));
        saveOrConflict(a);
        notifyBoth(a, "appointment.rescheduled", "Appointment rescheduled",
                "Your appointment with " + doctorName(a) + " has been moved to " + Notifier.when(newStart) + ".",
                patientName(a) + " moved their appointment to " + Notifier.when(newStart) + ".");
        return Mapper.toDto(a);
    }

    // ---------- Doctor actions ----------

    @Transactional(readOnly = true)
    public List<AppointmentDto> doctorDay(Long userId, LocalDate date) {
        Doctor doctor = currentDoctor(userId);
        return appointments.findForDoctorBetween(doctor.getId(), date.atStartOfDay(), date.plusDays(1).atStartOfDay())
                .stream().map(Mapper::toDto).toList();
    }

    @Transactional
    public AppointmentDto updateStatus(Long userId, Long id, StatusRequest req) {
        Doctor doctor = currentDoctor(userId);
        Appointment a = appointments.findById(id).orElseThrow(() -> ApiException.notFound("Appointment not found"));
        if (!a.getDoctor().getId().equals(doctor.getId())) throw ApiException.forbidden("This is not your appointment");
        if (a.getStatus() != AppointmentStatus.BOOKED) throw ApiException.badRequest("Only BOOKED appointments can be updated");
        if (req.status() != AppointmentStatus.COMPLETED && req.status() != AppointmentStatus.NO_SHOW)
            throw ApiException.badRequest("Status must be COMPLETED or NO_SHOW");
        a.setStatus(req.status());
        if (req.notes() != null && !req.notes().isBlank()) a.setNotes(req.notes().trim());
        if (req.status() == AppointmentStatus.COMPLETED) {
            notifier.send("appointment.completed", a.getPatient().getUser(), "Thank you for your visit",
                    "Thank you for visiting " + doctorName(a) + "."
                            + (a.getNotes() != null ? " Doctor's note: " + a.getNotes() : ""));
        }
        return Mapper.toDto(a);
    }

    // ---------- Helpers ----------

    /** One event for the patient (email + SMS) and one for the doctor. */
    private void notifyBoth(Appointment a, String type, String subject, String patientMessage, String doctorMessage) {
        notifier.send(type, a.getPatient().getUser(), subject, patientMessage);
        notifier.send(type, a.getDoctor().getUser(), subject, doctorMessage);
    }

    private static String doctorName(Appointment a) {
        return a.getDoctor().getUser().getFullName();
    }

    private static String patientName(Appointment a) {
        return a.getPatient().getUser().getFullName();
    }

    Patient currentPatient(Long userId) {
        return patients.findByUserId(userId).orElseThrow(() -> ApiException.forbidden("Only patients can do this"));
    }

    Doctor currentDoctor(Long userId) {
        return doctors.findByUserId(userId).orElseThrow(() -> ApiException.forbidden("Only doctors can do this"));
    }

    private Appointment ownedByPatient(Long userId, Long id) {
        Patient patient = currentPatient(userId);
        Appointment a = appointments.findById(id).orElseThrow(() -> ApiException.notFound("Appointment not found"));
        if (!a.getPatient().getId().equals(patient.getId())) throw ApiException.forbidden("This is not your appointment");
        return a;
    }

    private void requireChangeable(Appointment a, String action) {
        if (a.getStatus() != AppointmentStatus.BOOKED) throw ApiException.badRequest("Only booked appointments can be " + action);
        if (a.getStartTime().isBefore(LocalDateTime.now().plusHours(CHANGE_LIMIT_HOURS)))
            throw ApiException.badRequest("Appointments can only be " + action + " more than " + CHANGE_LIMIT_HOURS + " hours before they start.");
    }

    private void requireFreeSlot(Doctor doctor, LocalDateTime start) {
        boolean free = doctorService.slots(doctor.getId(), start.toLocalDate()).stream()
                .anyMatch(s -> s.startTime().equals(start) && s.available());
        if (!free) throw ApiException.conflict("This slot is not available. Please pick another one.");
    }

    /** The partial unique index uq_doctor_active_slot is the final guard against two people booking at once. */
    private void saveOrConflict(Appointment a) {
        try {
            appointments.saveAndFlush(a);
        } catch (DataIntegrityViolationException e) {
            throw ApiException.conflict("This slot was just taken. Please pick another one.");
        }
    }
}
