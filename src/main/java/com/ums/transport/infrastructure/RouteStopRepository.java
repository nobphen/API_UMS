package com.ums.transport.infrastructure;

import com.ums.transport.domain.RouteStop;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RouteStopRepository extends JpaRepository<RouteStop, Long> {
}
