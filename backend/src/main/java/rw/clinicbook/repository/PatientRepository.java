package rw.clinicbook.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import rw.clinicbook.domain.Patient;

public interface PatientRepository extends JpaRepository<Patient, Long> {

    Optional<Patient> findByUserId(Long userId);
}
