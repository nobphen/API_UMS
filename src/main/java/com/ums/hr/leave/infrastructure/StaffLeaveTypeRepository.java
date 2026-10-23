package com.ums.hr.leave.infrastructure;

import com.ums.hr.leave.domain.StaffLeaveType;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StaffLeaveTypeRepository extends JpaRepository<StaffLeaveType, Long> {
}
