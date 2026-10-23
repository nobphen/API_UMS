package com.ums.hostel.infrastructure;

import com.ums.hostel.domain.HostelRoom;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HostelRoomRepository extends JpaRepository<HostelRoom, Long> {
}
