package com.ums.transport.infrastructure;

import com.ums.transport.domain.TransportSubscription;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TransportSubscriptionRepository extends JpaRepository<TransportSubscription, Long> {
}
