package com.ums.academic.section.infrastructure;

import com.ums.academic.section.domain.ClassSection;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClassSectionRepository extends JpaRepository<ClassSection, Long> {
}
