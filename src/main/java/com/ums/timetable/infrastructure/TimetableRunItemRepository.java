package com.ums.timetable.infrastructure;

import com.ums.timetable.domain.TimetableRunItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TimetableRunItemRepository extends JpaRepository<TimetableRunItem, Long> {
}
