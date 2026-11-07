package com.ums.people.infrastructure;

import com.ums.people.domain.Batch;
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
}
