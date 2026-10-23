package com.ums.library.infrastructure;

import com.ums.library.domain.BookReservation;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookReservationRepository extends JpaRepository<BookReservation, Long> {
}
