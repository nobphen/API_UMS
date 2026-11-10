package com.ums.organization.infrastructure;

import com.ums.organization.domain.Program;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

/** Database access for programs (table programs). Spring Data writes the SQL from the method names. */
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

    /**
     * Whether any table still points to this program, so it can't be deleted. Plain SQL on purpose: the tables
     * belong to other modules (people, academic, admission, finance), and organization must not import their
     * repositories (Spring Modulith rule, see ModularityTest). Same pattern as BatchRepository.programExists.
     * Students: deleted ones are not counted. The other tables: every row counts, deleted ones too (history).
     *
     * @param programId program id
     * @return true when a student, batch, curriculum, intake, application or fee still uses the program
     */
    @Query(value = """
            SELECT EXISTS (SELECT 1 FROM students s WHERE s.program_id = :programId AND s.deleted_at IS NULL)
                OR EXISTS (SELECT 1 FROM batches b WHERE b.program_id = :programId)
                OR EXISTS (SELECT 1 FROM curriculums c WHERE c.program_id = :programId)
                OR EXISTS (SELECT 1 FROM intake_programs ip WHERE ip.program_id = :programId)
                OR EXISTS (SELECT 1 FROM applications a
                           WHERE a.first_choice_program_id = :programId
                              OR a.second_choice_program_id = :programId
                              OR a.admitted_program_id = :programId)
                OR EXISTS (SELECT 1 FROM fee_structures f WHERE f.program_id = :programId)
            """, nativeQuery = true)
    boolean isInUse(Long programId);
}
