package com.ums.hostel.infrastructure;

import com.ums.hostel.domain.HostelAllocation;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HostelAllocationRepository extends JpaRepository<HostelAllocation, Long> {
}
