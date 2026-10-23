package com.ums.hostel.infrastructure;

import com.ums.hostel.domain.HostelMaintenanceRequest;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HostelMaintenanceRequestRepository extends JpaRepository<HostelMaintenanceRequest, Long> {
}
