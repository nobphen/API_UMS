package com.ums.library.infrastructure;

import com.ums.library.domain.LibraryFine;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LibraryFineRepository extends JpaRepository<LibraryFine, Long> {
}
