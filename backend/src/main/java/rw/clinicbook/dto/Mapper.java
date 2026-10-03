package rw.clinicbook.dto;

import rw.clinicbook.domain.Appointment;
import rw.clinicbook.domain.Availability;
import rw.clinicbook.domain.Department;
import rw.clinicbook.domain.Doctor;

/** Converts entities to DTOs. Call inside a transaction (lazy relations). */
public final class Mapper {

    private Mapper() { }

    public static DepartmentDto toDto(Department d) {
        return new DepartmentDto(d.getId(), d.getName(), d.getDescription());
    }

    public static DoctorDto toDto(Doctor d) {
        return new DoctorDto(d.getId(), d.getUser().getFullName(), d.getUser().getEmail(), d.getSpecialization(),
                d.getLicenseNumber(), d.getDepartment().getId(), d.getDepartment().getName(), d.getSlotMinutes());
    }

    public static AppointmentDto toDto(Appointment a) {
        Doctor d = a.getDoctor();
        return new AppointmentDto(a.getId(), d.getId(), d.getUser().getFullName(), d.getDepartment().getName(),
                a.getPatient().getId(), a.getPatient().getUser().getFullName(),
                a.getStartTime(), a.getEndTime(), a.getStatus().name(), a.getReason(), a.getNotes());
    }

    public static AvailabilityDto toDto(Availability a) {
        return new AvailabilityDto(a.getId(), a.getDayOfWeek(), a.getStartTime(), a.getEndTime());
    }
}
