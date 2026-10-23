package com.ums.request.leave.infrastructure;

import com.ums.request.leave.domain.LeaveType;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LeaveTypeRepository extends JpaRepository<LeaveType, Long> {
}
