package com.ums.transport.infrastructure;

import com.ums.transport.domain.TransportRoute;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TransportRouteRepository extends JpaRepository<TransportRoute, Long> {
}
