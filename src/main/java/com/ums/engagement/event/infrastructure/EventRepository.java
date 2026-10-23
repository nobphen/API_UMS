package com.ums.engagement.event.infrastructure;

import com.ums.engagement.event.domain.Event;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EventRepository extends JpaRepository<Event, Long> {
}
