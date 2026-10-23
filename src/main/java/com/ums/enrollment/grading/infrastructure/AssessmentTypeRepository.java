package com.ums.enrollment.grading.infrastructure;

import com.ums.enrollment.grading.domain.AssessmentType;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AssessmentTypeRepository extends JpaRepository<AssessmentType, Long> {
}
