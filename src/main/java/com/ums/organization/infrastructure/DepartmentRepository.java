package com.ums.organization.infrastructure;

import com.ums.organization.domain.Department;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DepartmentRepository extends JpaRepository<Department, Long> {

    /**
     * Whether the faculty still has departments (deleted ones not counted).
     * <pre>
     * SELECT EXISTS (SELECT 1 FROM departments d WHERE d.faculty_id = ? AND d.deleted_at IS NULL)
     * </pre>
     *
     * @param facultyId faculty id
     * @return true when at least one department belongs to the faculty
     */
    boolean existsByFacultyIdAndDeletedAtIsNull(Long facultyId);
}
