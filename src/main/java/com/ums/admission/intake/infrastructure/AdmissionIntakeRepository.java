package com.ums.admission.intake.infrastructure;

import com.ums.admission.intake.domain.AdmissionIntake;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AdmissionIntakeRepository extends JpaRepository<AdmissionIntake, Long> {
}
