package rw.clinicbook.service;

import java.util.List;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rw.clinicbook.domain.Department;
import rw.clinicbook.dto.DepartmentDto;
import rw.clinicbook.dto.DepartmentRequest;
import rw.clinicbook.dto.Mapper;
import rw.clinicbook.exception.ApiException;
import rw.clinicbook.repository.DepartmentRepository;
import rw.clinicbook.repository.DoctorRepository;

@Service
public class DepartmentService {

    private final DepartmentRepository departments;
    private final DoctorRepository doctors;

    public DepartmentService(DepartmentRepository departments, DoctorRepository doctors) {
        this.departments = departments;
        this.doctors = doctors;
    }

    @Cacheable("departments")
    @Transactional(readOnly = true)
    public List<DepartmentDto> list() {
        return departments.findAllByOrderByNameAsc().stream().map(Mapper::toDto).toList();
    }

    @CacheEvict(value = "departments", allEntries = true)
    @Transactional
    public DepartmentDto create(DepartmentRequest req) {
        String name = req.name().trim();
        if (departments.existsByNameIgnoreCase(name)) throw ApiException.conflict("Department already exists");
        return Mapper.toDto(departments.save(new Department(name, req.description())));
    }

    @CacheEvict(value = "departments", allEntries = true)
    @Transactional
    public void delete(Long id) {
        if (!departments.existsById(id)) throw ApiException.notFound("Department not found");
        if (doctors.existsByDepartmentId(id)) throw ApiException.conflict("Cannot delete a department that still has doctors");
        departments.deleteById(id);
    }
}
