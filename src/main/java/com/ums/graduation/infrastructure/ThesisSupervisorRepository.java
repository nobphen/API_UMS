package com.ums.graduation.infrastructure;

import com.ums.graduation.domain.ThesisSupervisor;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ThesisSupervisorRepository extends JpaRepository<ThesisSupervisor, Long> {
}
