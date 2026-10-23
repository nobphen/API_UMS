package com.ums.engagement.alumni.infrastructure;

import com.ums.engagement.alumni.domain.AlumniProfile;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AlumniProfileRepository extends JpaRepository<AlumniProfile, Long> {
}
