package com.ums.request.studentrequest.infrastructure;

import com.ums.request.studentrequest.domain.RequestApproval;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RequestApprovalRepository extends JpaRepository<RequestApproval, Long> {
}
