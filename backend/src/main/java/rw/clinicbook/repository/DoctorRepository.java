package rw.clinicbook.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import rw.clinicbook.domain.Doctor;

public interface DoctorRepository extends JpaRepository<Doctor, Long> {

    Optional<Doctor> findByUserId(Long userId);

    boolean existsByDepartmentId(Long departmentId);

    boolean existsByLicenseNumberIgnoreCase(String licenseNumber);

    /** One query loads doctors with their user + department (avoids the N+1 problem). */
    @Query("select d from Doctor d join fetch d.user u join fetch d.department where u.enabled = true order by u.fullName")
    List<Doctor> findAllActiveWithDetails();
}
