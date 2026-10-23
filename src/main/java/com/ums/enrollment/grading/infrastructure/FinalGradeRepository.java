package com.ums.enrollment.grading.infrastructure;

import com.ums.enrollment.grading.domain.FinalGrade;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FinalGradeRepository extends JpaRepository<FinalGrade, Long> {
}
