package com.ums.enrollment.grading.infrastructure;

import com.ums.enrollment.grading.domain.Assessment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AssessmentRepository extends JpaRepository<Assessment, Long> {
}
