package com.ums.organization.infrastructure;

import com.ums.organization.domain.Program;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface ProgramRepository extends JpaRepository<Program, Long> {

    /**
     * One program, skipping deleted ones.
     * <pre>
     * SELECT p.* FROM programs p WHERE p.id = ? AND p.deleted_at IS NULL
     * </pre>
     *
     * @param id program id
     * @return the program, or empty when it does not exist or was deleted
     */
    Optional<Program> findByIdAndDeletedAtIsNull(Long id);

    /**
     * Whether a program already has this code, deleted ones included: the database keeps the code UNIQUE
     * even after a soft delete, so a deleted program's code can't be used again.
     * <pre>
     * SELECT EXISTS (SELECT 1 FROM programs p WHERE p.code = ?)
     * </pre>
     *
     * @param code program code, e.g. BCS
     * @return true when the code is taken
     */
    boolean existsByCode(String code);

    /**
     * Like {@link #existsByCode(String)}, but ignores the program being updated (it may keep its own code).
     * <pre>
     * SELECT EXISTS (SELECT 1 FROM programs p WHERE p.code = ? AND p.id <> ?)
     * </pre>
     *
     * @param code program code, e.g. BCS
     * @param id   id of the program being updated
     * @return true when another program has the code
     */
    boolean existsByCodeAndIdNot(String code, Long id);

    /**
     * Programs for the list page, skipping deleted ones. departmentId keeps only that department's programs
     * (null = all departments); q filters by code, English or Khmer name (empty q = all).
     * Sorting and page size come from the Pageable (?page=1&size=20&sort=name,asc).
     *
     * @param q            search text; "" = all
     * @param departmentId department id, or null for all departments
     * @param pageable     page number, size and sort
     * @return one page of programs, with the totals
     */
    @Query("""
            select p from Program p
            where p.deletedAt is null
              and (:departmentId is null or p.department.id = :departmentId)
              and (lower(p.code) like lower(concat('%', :q, '%'))
                   or lower(p.name) like lower(concat('%', :q, '%'))
                   or p.nameKh like concat('%', :q, '%'))
            """)
    Page<Program> search(String q, Long departmentId, Pageable pageable);

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
