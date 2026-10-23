package com.ums.timetable.infrastructure;

import com.ums.timetable.domain.TimetableConstraint;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TimetableConstraintRepository extends JpaRepository<TimetableConstraint, Long> {
}
