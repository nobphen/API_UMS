package com.ums.admission.application.infrastructure;

import com.ums.admission.application.domain.Application;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ApplicationRepository extends JpaRepository<Application, Long> {

    /**
     * Whether an application uses this program as first choice, second choice or admitted program.
     * Used by ProgramService.delete.
     * <pre>
     * SELECT EXISTS (SELECT 1 FROM applications a
     *                WHERE a.first_choice_program_id = ? OR a.second_choice_program_id = ? OR a.admitted_program_id = ?)
     * </pre>
     *
     * @param programId program id
     * @return true when at least one application uses the program
     */
    @Query("""
            select count(a) > 0 from Application a
            where a.firstChoiceProgramId = :programId
               or a.secondChoiceProgramId = :programId
               or a.admittedProgramId = :programId
            """)
    boolean existsByProgramId(Long programId);
}
