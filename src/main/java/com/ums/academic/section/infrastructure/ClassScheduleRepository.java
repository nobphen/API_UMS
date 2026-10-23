package com.ums.academic.section.infrastructure;

import com.ums.academic.section.domain.ClassSchedule;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClassScheduleRepository extends JpaRepository<ClassSchedule, Long> {
}
