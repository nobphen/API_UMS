package com.ums.engagement.event.infrastructure;

import com.ums.engagement.event.domain.EventRegistration;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EventRegistrationRepository extends JpaRepository<EventRegistration, Long> {
}
