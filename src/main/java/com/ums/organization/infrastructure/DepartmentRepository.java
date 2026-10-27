package com.ums.organization.infrastructure;
import com.ums.organization.domain.Department;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface DepartmentRepository extends JpaRepository<Department, Long> {

    /**
     * One department, skipping deleted ones.
     * <pre>
     * SELECT d.* FROM departments d WHERE d.id = ? AND d.deleted_at IS NULL
     * </pre>
     *
     * @param id department id
     * @return the department, or empty when it does not exist or was deleted
     */
    Optional<Department> findByIdAndDeletedAtIsNull(Long id);

    /**
     * Whether a department already has this code, deleted ones included: the database keeps the code UNIQUE
     * even after a soft delete, so a deleted department's code can't be used again.
     * <pre>
     * SELECT EXISTS (SELECT 1 FROM departments d WHERE d.code = ?)
     * </pre>
     *
     * @param code department code, e.g. CS
     * @return true when the code is taken
     */
    boolean existsByCode(String code);

    /**
     * Like {@link #existsByCode(String)}, but ignores the department being updated (it may keep its own code).
     * <pre>
     * SELECT EXISTS (SELECT 1 FROM departments d WHERE d.code = ? AND d.id <> ?)
     * </pre>
     *
     * @param code department code, e.g. CS
     * @param id   id of the department being updated
     * @return true when another department has the code
     */
    boolean existsByCodeAndIdNot(String code, Long id);

    /**
     * Departments for the list page, skipping deleted ones. facultyId keeps only that faculty's departments
     * (null = all faculties); q filters by code, English or Khmer name (empty q = all).
     * Sorting and page size come from the Pageable (?page=1&size=20&sort=name,asc).
     *
     * @param q         search text; "" = all
     * @param facultyId faculty id, or null for all faculties
     * @param pageable  page number, size and sort
     * @return one page of departments, with the totals
     */
    @Query("""
            select d from Department d
            where d.deletedAt is null
              and (:facultyId is null or d.faculty.id = :facultyId)
              and (lower(d.code) like lower(concat('%', :q, '%'))
                   or lower(d.name) like lower(concat('%', :q, '%'))
                   or d.nameKh like concat('%', :q, '%'))
            """)
    Page<Department> search(String q, Long facultyId, Pageable pageable);

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
