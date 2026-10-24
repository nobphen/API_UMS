package com.ums.graduation.infrastructure;

import com.ums.graduation.domain.Thesis;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ThesisRepository extends JpaRepository<Thesis, Long> {
}
