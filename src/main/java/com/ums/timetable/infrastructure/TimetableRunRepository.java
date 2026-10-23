package com.ums.timetable.infrastructure;

import com.ums.timetable.domain.TimetableRun;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TimetableRunRepository extends JpaRepository<TimetableRun, Long> {
}
