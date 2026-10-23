package com.ums.academic.calendar.infrastructure;

import com.ums.academic.calendar.domain.Semester;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SemesterRepository extends JpaRepository<Semester, Long> {
}
