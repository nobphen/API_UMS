package com.ums.enrollment.attendance.infrastructure;

import com.ums.enrollment.attendance.domain.AttendanceRecord;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AttendanceRecordRepository extends JpaRepository<AttendanceRecord, Long> {
}
