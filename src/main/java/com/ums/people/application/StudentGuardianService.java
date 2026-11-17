package com.ums.people.application;

import com.ums.people.api.dto.StudentGuardianRequest;
import com.ums.people.api.dto.StudentGuardianResponse;
import com.ums.people.api.dto.StudentGuardianUpdateRequest;
import java.util.List;

/**
 * Links students to their guardians (table student_guardians): who is the father, mother ...,
 * and which guardian the school calls first (primary, only one per student).
 * The guardian itself is added with GuardianService first; here only the link is made.
 */
public interface StudentGuardianService {

    /**
     * Links a guardian to a student. The student's first guardian is always primary;
     * a new primary guardian takes the place of the old one.
     *
     * @param studentId student id
     * @param request   guardian id, relationship, primary
     * @return the new link with the guardian's details
     * @throws com.ums.common.exception.NotFoundException error.student.not_found / error.guardian.not_found
     *                                                    when one of them does not exist or was deleted
     * @throws com.ums.common.exception.BusinessException error.student_guardian.exists when they are already linked
     */
    StudentGuardianResponse link(Long studentId, StudentGuardianRequest request);

    /**
     * The student's guardians, primary first.
     *
     * @param studentId student id
     * @return the guardians; empty when none are linked
     * @throws com.ums.common.exception.NotFoundException error.student.not_found when it does not exist or was deleted
     */
    List<StudentGuardianResponse> list(Long studentId);

    /**
     * Changes one link: relationship and primary.
     *
     * @param studentId  student id
     * @param guardianId guardian id
     * @param request    relationship, primary
     * @return the changed link
     * @throws com.ums.common.exception.NotFoundException error.student_guardian.not_found when they are not linked
     */
    StudentGuardianResponse update(Long studentId, Long guardianId, StudentGuardianUpdateRequest request);

    /**
     * Removes the link only; the guardian stays (they may have other children at the school).
     *
     * @param studentId  student id
     * @param guardianId guardian id
     * @throws com.ums.common.exception.NotFoundException error.student_guardian.not_found when they are not linked
     */
    void unlink(Long studentId, Long guardianId);
}
