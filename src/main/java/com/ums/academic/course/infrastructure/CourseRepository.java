package com.ums.academic.course.infrastructure;

import com.ums.academic.course.domain.Course;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CourseRepository extends JpaRepository<Course, Long> {
}
