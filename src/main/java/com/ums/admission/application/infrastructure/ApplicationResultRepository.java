package com.ums.admission.application.infrastructure;

import com.ums.admission.application.domain.ApplicationResult;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ApplicationResultRepository extends JpaRepository<ApplicationResult, Long> {
}
