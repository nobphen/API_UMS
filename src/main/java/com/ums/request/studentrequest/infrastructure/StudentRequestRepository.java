package com.ums.request.studentrequest.infrastructure;

import com.ums.request.studentrequest.domain.StudentRequest;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StudentRequestRepository extends JpaRepository<StudentRequest, Long> {
}
