package com.ums.organization.infrastructure;

import com.ums.organization.domain.Program;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProgramRepository extends JpaRepository<Program, Long> {

    /**
     * Whether the department still has programs (deleted ones not counted).
     * <pre>
     * SELECT EXISTS (SELECT 1 FROM programs p WHERE p.department_id = ? AND p.deleted_at IS NULL)
     * </pre>
     *
     * @param departmentId department id
     * @return true when at least one program belongs to the department
     */
    boolean existsByDepartmentIdAndDeletedAtIsNull(Long departmentId);
}
