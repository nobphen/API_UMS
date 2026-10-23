package com.ums.hr.leave.infrastructure;

import com.ums.hr.leave.domain.StaffLeaveRequest;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StaffLeaveRequestRepository extends JpaRepository<StaffLeaveRequest, Long> {
}
