package rw.clinicbook.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import rw.clinicbook.domain.Appointment;
import rw.clinicbook.domain.AppointmentStatus;

public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    @Query("""
           select a from Appointment a
           join fetch a.doctor d join fetch d.user join fetch d.department
           where a.patient.id = :patientId
           order by a.startTime desc
           """)
    List<Appointment> findForPatient(@Param("patientId") Long patientId);

    @Query("""
           select a from Appointment a
           join fetch a.patient p join fetch p.user
           where a.doctor.id = :doctorId and a.startTime >= :from and a.startTime < :to
           order by a.startTime
           """)
    List<Appointment> findForDoctorBetween(@Param("doctorId") Long doctorId,
                                           @Param("from") LocalDateTime from,
                                           @Param("to") LocalDateTime to);

    boolean existsByDoctorIdAndStatus(Long doctorId, AppointmentStatus status);

    /** True if the user is the patient or the doctor of this appointment. */
    @Query("""
           select case when count(a) > 0 then true else false end from Appointment a
           where a.id = :appointmentId and (a.patient.user.id = :userId or a.doctor.user.id = :userId)
           """)
    boolean isParticipant(@Param("appointmentId") Long appointmentId, @Param("userId") Long userId);

    @Query("select a.patient.id from Appointment a where a.id = :appointmentId")
    Optional<Long> findPatientId(@Param("appointmentId") Long appointmentId);
}
