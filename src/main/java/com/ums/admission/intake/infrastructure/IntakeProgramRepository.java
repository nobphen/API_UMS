package com.ums.admission.intake.infrastructure;

import com.ums.admission.intake.domain.IntakeProgram;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IntakeProgramRepository extends JpaRepository<IntakeProgram, Long> {

    /**
     * Whether an intake offers this program. Used by ProgramService.delete.
     * <pre>
     * SELECT EXISTS (SELECT 1 FROM intake_programs ip WHERE ip.program_id = ?)
     * </pre>
     *
     * @param programId program id
     * @return true when at least one intake offers the program
     */
    boolean existsByProgramId(Long programId);
}
