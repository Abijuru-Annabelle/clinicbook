package rw.clinicbook.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;
import rw.clinicbook.domain.*;
import rw.clinicbook.dto.AppointmentDto;
import rw.clinicbook.exception.ApiException;
import rw.clinicbook.messaging.Notifier;
import rw.clinicbook.repository.AppointmentRepository;
import rw.clinicbook.repository.DoctorRepository;
import rw.clinicbook.repository.PatientRepository;

/** Unit tests for the business rules of cancelling appointments (pure Mockito, no Spring). */
class AppointmentRulesTest {

    private AppointmentRepository appointments;
    private PatientRepository patients;
    private Notifier notifier;
    private AppointmentService service;

    private Patient me;
    private Patient someoneElse;
    private Doctor doctor;

    @BeforeEach
    void setUp() {
        appointments = mock(AppointmentRepository.class);
        patients = mock(PatientRepository.class);
        notifier = mock(Notifier.class);
        service = new AppointmentService(appointments, patients, mock(DoctorRepository.class),
                mock(DoctorService.class), notifier);

        me = patient(1L, 10L, "Kendra");
        someoneElse = patient(2L, 20L, "Other Patient");

        User doctorUser = user(30L, "Dr. Aline Uwase");
        doctor = new Doctor();
        doctor.setUser(doctorUser);
        doctor.setDepartment(new Department("Pediatrics", "Children"));
        doctor.setSlotMinutes(30);

        when(patients.findByUserId(10L)).thenReturn(Optional.of(me));
    }

    @Test
    @DisplayName("Patient can cancel more than 2 hours before -> CANCELLED and both sides notified")
    void cancelInTime() {
        Appointment a = appointment(me, LocalDateTime.now().plusDays(2));
        when(appointments.findById(5L)).thenReturn(Optional.of(a));

        AppointmentDto result = service.cancel(10L, 5L);

        assertThat(result.status()).isEqualTo("CANCELLED");
        verify(notifier, times(2)).send(eq("appointment.cancelled"), any(), any(), any());
    }

    @Test
    @DisplayName("Cancelling less than 2 hours before is refused (400)")
    void cancelTooLate() {
        Appointment a = appointment(me, LocalDateTime.now().plusMinutes(90));
        when(appointments.findById(5L)).thenReturn(Optional.of(a));

        assertThatThrownBy(() -> service.cancel(10L, 5L))
                .isInstanceOf(ApiException.class)
                .extracting(e -> ((ApiException) e).getStatus()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(a.getStatus()).isEqualTo(AppointmentStatus.BOOKED);
        verify(notifier, never()).send(any(), any(), any(), any());
    }

    @Test
    @DisplayName("A patient cannot cancel another patient's appointment (403)")
    void cannotCancelOthers() {
        Appointment a = appointment(someoneElse, LocalDateTime.now().plusDays(2));
        when(appointments.findById(5L)).thenReturn(Optional.of(a));

        assertThatThrownBy(() -> service.cancel(10L, 5L))
                .isInstanceOf(ApiException.class)
                .extracting(e -> ((ApiException) e).getStatus()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(a.getStatus()).isEqualTo(AppointmentStatus.BOOKED);
    }

    @Test
    @DisplayName("An already cancelled appointment cannot be cancelled again (400)")
    void cannotCancelTwice() {
        Appointment a = appointment(me, LocalDateTime.now().plusDays(2));
        a.setStatus(AppointmentStatus.CANCELLED);
        when(appointments.findById(5L)).thenReturn(Optional.of(a));

        assertThatThrownBy(() -> service.cancel(10L, 5L)).isInstanceOf(ApiException.class);
    }

    // ---------- helpers ----------

    private static User user(Long id, String name) {
        User u = new User();
        ReflectionTestUtils.setField(u, "id", id);
        u.setFullName(name);
        u.setEmail(name.toLowerCase().replace(" ", ".") + "@test.rw");
        u.setRole(Role.PATIENT);
        return u;
    }

    private static Patient patient(Long id, Long userId, String name) {
        Patient p = new Patient(user(userId, name));
        ReflectionTestUtils.setField(p, "id", id);
        return p;
    }

    private Appointment appointment(Patient p, LocalDateTime start) {
        Appointment a = new Appointment();
        a.setPatient(p);
        a.setDoctor(doctor);
        a.setStartTime(start);
        a.setEndTime(start.plusMinutes(30));
        return a;
    }
}
