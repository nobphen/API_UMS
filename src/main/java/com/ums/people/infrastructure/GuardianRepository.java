package com.ums.people.infrastructure;

import com.ums.people.domain.Guardian;
import org.springframework.data.jpa.repository.JpaRepository;

/** Database access for guardians (table guardians). Only the basic JpaRepository methods for now. */
public interface GuardianRepository extends JpaRepository<Guardian, Long> {
}
