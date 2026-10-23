package com.ums.timetable.infrastructure;

import com.ums.timetable.domain.LecturerAvailability;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LecturerAvailabilityRepository extends JpaRepository<LecturerAvailability, Long> {
}
