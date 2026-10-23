package com.ums.exam.infrastructure;

import com.ums.exam.domain.ExamSchedule;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ExamScheduleRepository extends JpaRepository<ExamSchedule, Long> {
}
