package rw.clinicbook.config;

import java.time.LocalTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import rw.clinicbook.domain.*;
import rw.clinicbook.repository.*;

/** Inserts demo data the first time the app starts (empty database). */
@Component
public class DataSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

    private final UserRepository users;
    private final PatientRepository patients;
    private final DoctorRepository doctors;
    private final DepartmentRepository departments;
    private final AvailabilityRepository availability;
    private final PasswordEncoder encoder;

    public DataSeeder(UserRepository users, PatientRepository patients, DoctorRepository doctors,
                      DepartmentRepository departments, AvailabilityRepository availability, PasswordEncoder encoder) {
        this.users = users;
        this.patients = patients;
        this.doctors = doctors;
        this.departments = departments;
        this.availability = availability;
        this.encoder = encoder;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (users.count() > 0) return;

        user("Clinic Admin", "admin@clinicbook.rw", "+250780000001", "Admin@123", Role.ADMIN);
        patients.save(new Patient(user("Demo Patient", "patient@clinicbook.rw", "+250780000002", "Patient@123", Role.PATIENT)));

        Department general = departments.save(new Department("General Medicine", "Consultations for common illnesses"));
        Department pediatrics = departments.save(new Department("Pediatrics", "Care for children and infants"));
        Department dentistry = departments.save(new Department("Dentistry", "Oral and dental care"));
        Department gynecology = departments.save(new Department("Gynecology", "Women's health"));

        doctor("Dr. Eric Mugisha", "doctor@clinicbook.rw", general, "General Practitioner", "RMDC-1001", 30);
        doctor("Dr. Aline Uwase", "aline@clinicbook.rw", pediatrics, "Pediatrician", "RMDC-1002", 30);
        doctor("Dr. Jean Habimana", "jean@clinicbook.rw", dentistry, "Dentist", "RMDC-1003", 45);
        doctor("Dr. Grace Mukamana", "grace@clinicbook.rw", gynecology, "Gynecologist", "RMDC-1004", 30);

        log.info("Demo data created. Logins: admin@clinicbook.rw/Admin@123, patient@clinicbook.rw/Patient@123, doctor@clinicbook.rw/Doctor@123");
    }

    private User user(String name, String email, String phone, String password, Role role) {
        User u = new User();
        u.setFullName(name);
        u.setEmail(email);
        u.setPhone(phone);
        u.setPasswordHash(encoder.encode(password));
        u.setRole(role);
        return users.save(u);
    }

    private void doctor(String name, String email, Department dept, String specialization, String license, int slot) {
        Doctor d = new Doctor();
        d.setUser(user(name, email, null, "Doctor@123", Role.DOCTOR));
        d.setDepartment(dept);
        d.setSpecialization(specialization);
        d.setLicenseNumber(license);
        d.setSlotMinutes(slot);
        doctors.save(d);
        for (int day = 1; day <= 5; day++) {
            availability.save(new Availability(d, day, LocalTime.of(8, 0), LocalTime.of(12, 0)));
            availability.save(new Availability(d, day, LocalTime.of(14, 0), LocalTime.of(17, 0)));
        }
        availability.save(new Availability(d, 6, LocalTime.of(9, 0), LocalTime.of(12, 0)));
    }
}
