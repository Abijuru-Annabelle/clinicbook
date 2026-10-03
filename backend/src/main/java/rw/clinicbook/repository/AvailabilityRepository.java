package rw.clinicbook.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import rw.clinicbook.domain.Availability;

public interface AvailabilityRepository extends JpaRepository<Availability, Long> {

    List<Availability> findByDoctorIdOrderByDayOfWeekAscStartTimeAsc(Long doctorId);

    List<Availability> findByDoctorIdAndDayOfWeekOrderByStartTimeAsc(Long doctorId, Integer dayOfWeek);
}
