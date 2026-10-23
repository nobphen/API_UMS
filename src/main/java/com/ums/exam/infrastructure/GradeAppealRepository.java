package com.ums.exam.infrastructure;

import com.ums.exam.domain.GradeAppeal;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GradeAppealRepository extends JpaRepository<GradeAppeal, Long> {
}
