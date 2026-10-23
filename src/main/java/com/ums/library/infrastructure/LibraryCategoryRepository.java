package com.ums.library.infrastructure;

import com.ums.library.domain.LibraryCategory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LibraryCategoryRepository extends JpaRepository<LibraryCategory, Long> {
}
