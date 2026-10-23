package com.ums.enrollment.attendance.infrastructure;

import com.ums.enrollment.attendance.domain.AttendanceSession;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AttendanceSessionRepository extends JpaRepository<AttendanceSession, Long> {
}
