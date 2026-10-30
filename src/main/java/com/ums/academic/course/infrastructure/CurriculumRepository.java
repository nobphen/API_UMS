package com.ums.academic.course.infrastructure;

import com.ums.academic.course.domain.Curriculum;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CurriculumRepository extends JpaRepository<Curriculum, Long> {

    /**
     * Whether a curriculum belongs to this program. Used by ProgramService.delete.
     * <pre>
     * SELECT EXISTS (SELECT 1 FROM curriculums c WHERE c.program_id = ?)
     * </pre>
     *
     * @param programId program id
     * @return true when at least one curriculum belongs to the program
     */
    boolean existsByProgramId(Long programId);
}
