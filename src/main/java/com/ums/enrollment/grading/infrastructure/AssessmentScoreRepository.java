package com.ums.enrollment.grading.infrastructure;

import com.ums.enrollment.grading.domain.AssessmentScore;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AssessmentScoreRepository extends JpaRepository<AssessmentScore, Long> {
}
