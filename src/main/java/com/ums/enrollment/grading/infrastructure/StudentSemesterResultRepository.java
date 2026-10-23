package com.ums.enrollment.grading.infrastructure;

import com.ums.enrollment.grading.domain.StudentSemesterResult;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StudentSemesterResultRepository extends JpaRepository<StudentSemesterResult, Long> {
}
