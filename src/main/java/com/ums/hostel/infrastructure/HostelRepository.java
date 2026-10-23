package com.ums.hostel.infrastructure;

import com.ums.hostel.domain.Hostel;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HostelRepository extends JpaRepository<Hostel, Long> {
}
