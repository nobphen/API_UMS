package com.ums.request.leave.infrastructure;

import com.ums.request.leave.domain.LeaveRequestClass;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LeaveRequestClassRepository extends JpaRepository<LeaveRequestClass, Long> {
}
