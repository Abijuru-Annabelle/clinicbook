package rw.clinicbook.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import rw.clinicbook.domain.Department;

public interface DepartmentRepository extends JpaRepository<Department, Long> {

    boolean existsByNameIgnoreCase(String name);

    List<Department> findAllByOrderByNameAsc();
}
