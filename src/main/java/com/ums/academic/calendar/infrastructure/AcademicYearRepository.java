package com.ums.academic.calendar.infrastructure;

import com.ums.academic.calendar.domain.AcademicYear;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AcademicYearRepository extends JpaRepository<AcademicYear, Long> {
}
