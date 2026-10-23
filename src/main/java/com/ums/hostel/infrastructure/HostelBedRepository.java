package com.ums.hostel.infrastructure;

import com.ums.hostel.domain.HostelBed;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HostelBedRepository extends JpaRepository<HostelBed, Long> {
}
