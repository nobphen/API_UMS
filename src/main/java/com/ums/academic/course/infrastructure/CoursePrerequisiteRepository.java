package com.ums.academic.course.infrastructure;

import com.ums.academic.course.domain.CoursePrerequisite;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CoursePrerequisiteRepository extends JpaRepository<CoursePrerequisite, CoursePrerequisite.Id> {
}
