package com.ums.admission.application.infrastructure;

import com.ums.admission.application.domain.Applicant;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ApplicantRepository extends JpaRepository<Applicant, Long> {
}
