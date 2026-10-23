package com.ums.exam.infrastructure;

import com.ums.exam.domain.ExamInvigilator;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ExamInvigilatorRepository extends JpaRepository<ExamInvigilator, ExamInvigilator.Id> {
}
