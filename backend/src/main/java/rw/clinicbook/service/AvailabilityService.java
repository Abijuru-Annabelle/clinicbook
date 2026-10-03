package rw.clinicbook.service;

import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rw.clinicbook.domain.Availability;
import rw.clinicbook.domain.Doctor;
import rw.clinicbook.dto.AvailabilityDto;
import rw.clinicbook.dto.AvailabilityRequest;
import rw.clinicbook.dto.Mapper;
import rw.clinicbook.exception.ApiException;
import rw.clinicbook.repository.AvailabilityRepository;
import rw.clinicbook.repository.DoctorRepository;

@Service
public class AvailabilityService {

    private final AvailabilityRepository availability;
    private final DoctorRepository doctors;

    public AvailabilityService(AvailabilityRepository availability, DoctorRepository doctors) {
        this.availability = availability;
        this.doctors = doctors;
    }

    @Transactional(readOnly = true)
    public List<AvailabilityDto> mine(Long userId) {
        return availability.findByDoctorIdOrderByDayOfWeekAscStartTimeAsc(currentDoctor(userId).getId())
                .stream().map(Mapper::toDto).toList();
    }

    @Transactional
    public AvailabilityDto add(Long userId, AvailabilityRequest req) {
        Doctor doctor = currentDoctor(userId);
        if (!req.endTime().isAfter(req.startTime())) throw ApiException.badRequest("End time must be after start time");
        boolean overlaps = availability.findByDoctorIdAndDayOfWeekOrderByStartTimeAsc(doctor.getId(), req.dayOfWeek())
                .stream()
                .anyMatch(w -> req.startTime().isBefore(w.getEndTime()) && req.endTime().isAfter(w.getStartTime()));
        if (overlaps) throw ApiException.conflict("This time overlaps with existing availability");
        return Mapper.toDto(availability.save(new Availability(doctor, req.dayOfWeek(), req.startTime(), req.endTime())));
    }

    @Transactional
    public void delete(Long userId, Long id) {
        Doctor doctor = currentDoctor(userId);
        Availability a = availability.findById(id).orElseThrow(() -> ApiException.notFound("Availability not found"));
        if (!a.getDoctor().getId().equals(doctor.getId())) throw ApiException.forbidden("This is not your availability");
        availability.delete(a);
    }

    private Doctor currentDoctor(Long userId) {
        return doctors.findByUserId(userId).orElseThrow(() -> ApiException.forbidden("Only doctors can do this"));
    }
}
