package com.ums.library.infrastructure;

import com.ums.library.domain.BookLoan;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookLoanRepository extends JpaRepository<BookLoan, Long> {
}
