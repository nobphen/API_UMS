package com.ums.graduation.infrastructure;

import com.ums.graduation.domain.TranscriptRequest;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TranscriptRequestRepository extends JpaRepository<TranscriptRequest, Long> {
}
