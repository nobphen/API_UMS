package com.ums.people.infrastructure;

import com.ums.people.domain.Batch;
import org.springframework.data.jpa.repository.JpaRepository;

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
}
