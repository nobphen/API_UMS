package com.ums.admission.application.infrastructure;

import com.ums.admission.application.domain.ApplicationDocument;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ApplicationDocumentRepository extends JpaRepository<ApplicationDocument, Long> {
}
