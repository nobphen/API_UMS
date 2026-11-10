package com.ums.people.infrastructure;

import com.ums.people.domain.Batch;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/** Database access for batches (table batches). */
public interface BatchRepository extends JpaRepository<Batch, Long> {

    /**
     * Whether a batch belongs to this program. Used by ProgramService.delete.
     * <pre>
     * SELECT EXISTS (SELECT 1 FROM batches b WHERE b.program_id = ?)
     * </pre>
     *
     * @param programId program id
     * @return true when at least one batch belongs to the program
     */
    boolean existsByProgramId(Long programId);

    /**
     * Whether this batch exists, is not deleted and belongs to this program. Used by StudentService.
     * <pre>
     * SELECT b.id FROM batches b WHERE b.id = ? AND b.program_id = ? AND b.deleted_at IS NULL FETCH FIRST 1 ROWS ONLY
     * </pre>
     *
     * @param id        batch id
     * @param programId program id
     * @return true when the batch can be used for a student of this program
     */
    boolean existsByIdAndProgramIdAndDeletedAtIsNull(Long id, Long programId);

    /**
     * Whether the program exists and is not deleted. Plain SQL on purpose: the program classes are in the
     * organization module, which already uses people, so importing them here would make a module cycle.
     * <pre>
     * SELECT EXISTS (SELECT 1 FROM programs p WHERE p.id = ? AND p.deleted_at IS NULL)
     * </pre>
     *
     * @param programId program id
     * @return true when students can be added to the program
     */
    @Query(value = "SELECT EXISTS (SELECT 1 FROM programs p WHERE p.id = :programId AND p.deleted_at IS NULL)",
            nativeQuery = true)
    boolean programExists(@Param("programId") Long programId);

    /**
     * One batch that is not deleted. Used by BatchService get, update and delete (empty = 404).
     * <pre>
     * SELECT b.* FROM batches b WHERE b.id = ? AND b.deleted_at IS NULL
     * </pre>
     *
     * @param id batch id
     * @return the batch, or empty when it does not exist or is deleted
     */
    Optional<Batch> findByIdAndDeletedAtIsNull(Long id);

    /**
     * Whether the program already has a batch with this name, ignoring case. Used by BatchService.create.
     * Deleted batches ARE counted on purpose: the database rule UNIQUE (program_id, name) counts them too.
     * <pre>
     * SELECT b.id FROM batches b WHERE b.program_id = ? AND upper(b.name) = upper(?) FETCH FIRST 1 ROWS ONLY
     * </pre>
     *
     * @param programId program id
     * @param name      batch name to check
     * @return true when the name is taken in this program
     */
    boolean existsByProgramIdAndNameIgnoreCase(Long programId, String name);

    /**
     * Same as {@link #existsByProgramIdAndNameIgnoreCase}, but skips the batch being changed.
     * Used by BatchService.update, so saving the same name again is not an error.
     * <pre>
     * SELECT b.id FROM batches b WHERE b.program_id = ? AND upper(b.name) = upper(?) AND b.id <> ? FETCH FIRST 1 ROWS ONLY
     * </pre>
     *
     * @param programId program id
     * @param name      batch name to check
     * @param id        the batch being changed (not counted)
     * @return true when another batch of this program uses the name
     */
    boolean existsByProgramIdAndNameIgnoreCaseAndIdNot(Long programId, String name, Long id);

    /**
     * All batches of one program that are not deleted, newest first. No paging: a program has only about one batch a year.
     * <pre>
     * SELECT b.* FROM batches b WHERE b.program_id = ? AND b.deleted_at IS NULL
     * ORDER BY b.start_year DESC, b.name ASC
     * </pre>
     *
     * @param programId program id
     * @return the program's batches, newest start year first
     */
    List<Batch> findByProgramIdAndDeletedAtIsNullOrderByStartYearDescNameAsc(Long programId);
}
