package com.ums.finance.fee.infrastructure;

import com.ums.finance.fee.domain.FeeStructure;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FeeStructureRepository extends JpaRepository<FeeStructure, Long> {

    /**
     * Whether a fee structure (price) is set for this program. Used by ProgramService.delete.
     * <pre>
     * SELECT EXISTS (SELECT 1 FROM fee_structures f WHERE f.program_id = ?)
     * </pre>
     *
     * @param programId program id
     * @return true when at least one fee structure uses the program
     */
    boolean existsByProgramId(Long programId);
}
