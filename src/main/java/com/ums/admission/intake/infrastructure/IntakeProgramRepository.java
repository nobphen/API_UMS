package com.ums.admission.intake.infrastructure;

import com.ums.admission.intake.domain.IntakeProgram;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IntakeProgramRepository extends JpaRepository<IntakeProgram, Long> {
}
