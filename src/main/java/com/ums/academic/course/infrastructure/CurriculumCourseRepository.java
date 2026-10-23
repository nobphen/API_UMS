package com.ums.academic.course.infrastructure;

import com.ums.academic.course.domain.CurriculumCourse;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CurriculumCourseRepository extends JpaRepository<CurriculumCourse, CurriculumCourse.Id> {
}
