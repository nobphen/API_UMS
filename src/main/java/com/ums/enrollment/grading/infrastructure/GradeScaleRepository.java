package com.ums.enrollment.grading.infrastructure;

import com.ums.enrollment.grading.domain.GradeScale;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GradeScaleRepository extends JpaRepository<GradeScale, Long> {
}
