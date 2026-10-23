package com.ums.people.infrastructure;

import com.ums.people.domain.Staff;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StaffRepository extends JpaRepository<Staff, Long> {

    Optional<Staff> findByUserIdAndDeletedAtIsNull(Long userId);
}
