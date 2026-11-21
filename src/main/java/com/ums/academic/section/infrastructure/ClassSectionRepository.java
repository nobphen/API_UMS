package com.ums.academic.section.infrastructure;

import com.ums.academic.section.domain.ClassSection;
import org.springframework.data.jpa.repository.JpaRepository;

/** Database access for class sections (table class_sections: one class of a course in a semester). */
public interface ClassSectionRepository extends JpaRepository<ClassSection, Long> {

    /**
     * Whether the semester still has a class section (not deleted). Used by SemesterService.delete:
     * a semester with classes can't be deleted.
     * <pre>
     * SELECT c.id FROM class_sections c WHERE c.semester_id = ? AND c.deleted_at IS NULL FETCH FIRST 1 ROWS ONLY
     * </pre>
     *
     * @param semesterId semester id
     * @return true when at least one class section uses the semester
     */
    boolean existsBySemester_IdAndDeletedAtIsNull(Long semesterId);
}
