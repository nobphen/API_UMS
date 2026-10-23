package com.ums.exam.infrastructure;

import com.ums.exam.domain.ExamSeatAssignment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ExamSeatAssignmentRepository extends JpaRepository<ExamSeatAssignment, Long> {
}
