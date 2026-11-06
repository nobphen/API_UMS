package com.ums.people.application;

import com.ums.people.api.dto.GuardianResponse;
import com.ums.people.api.dto.NewProgramRequest;
import com.ums.people.api.dto.NewStudentResponse;
import com.ums.people.api.dto.PromoteStudentsRequest;
import com.ums.people.api.dto.PromoteStudentsResponse;
import com.ums.people.api.dto.StudentRequest;
import com.ums.people.api.dto.StudentResponse;
import com.ums.people.api.dto.StudentStatusRequest;
import com.ums.people.api.dto.StudentUpdateRequest;
import com.ums.people.domain.Student;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/** Students: adding them (with their login), starting another program, moving up a year. Used by StudentController. */
public interface StudentService {

    /**
     * Adds the student and their login account (username = student code, temporary password).
     * With existingUserId, the existing login and person are reused instead and no password is made.
     *
     * @param request person details, addresses, student code, program, batch, admission date
     * @return the student, the username and the temporary password (null for an existing login)
     * @throws com.ums.common.exception.BusinessException error.student.code_exists when the code is taken;
     *                                                    error.student.already_in_program (existing login);
     *                                                    error.country.unknown for a bad nationality;
     *                                                    error.user.email_taken / phone_taken for the login;
     *                                                    error.address.* for a wrong address
     * @throws com.ums.common.exception.NotFoundException error.user.not_found when existingUserId does not exist;
     *                                                    error.geo.not_found for an unknown areaId
     */
    NewStudentResponse create(StudentRequest request);

    /**
     * The person behind an existing student record starts another program (Master after Bachelor, PhD after
     * Master, or a second degree). Same person and login, new student record; no new password.
     *
     * @param studentId id of any student record of the person
     * @param request   new student code, program, batch, admission date
     * @return the new student record and the username (temporaryPassword is null)
     * @throws com.ums.common.exception.NotFoundException error.student.not_found when the student does not exist
     * @throws com.ums.common.exception.BusinessException error.student.no_login when the student has no login;
     *                                                    error.student.code_exists when the code is taken;
     *                                                    error.student.already_in_program when already studying it
     */
    NewStudentResponse addProgram(Long studentId, NewProgramRequest request);

    /**
     * One student with person details and addresses.
     *
     * @param id student id
     * @return the student
     * @throws com.ums.common.exception.NotFoundException error.student.not_found when it does not exist or was deleted
     */
    StudentResponse get(Long id);

    /**
     * End of the academic year: ACTIVE students of the program (and batch) in fromYear move up one year,
     * except the ones kept back.
     *
     * @param request program, batch (null = all batches), fromYear, ids of the students repeating the year
     * @return how many moved up and how many were kept
     */
    PromoteStudentsResponse promote(PromoteStudentsRequest request);

    /**
     * Sets one student's year by hand, e.g. after a credit transfer or a mistake.
     *
     * @param id        student id
     * @param yearLevel the new year, 1 to 10
     * @return the student after the change
     * @throws com.ums.common.exception.NotFoundException error.student.not_found when it does not exist or was deleted
     */
    StudentResponse setYearLevel(Long id, int yearLevel);

    /**
     * Fixes a mistyped student code. The login's username changes too when it was the old code, so the student
     * logs in with the new code (same password). Only while still studying (ACTIVE, SUSPENDED, ON_LEAVE):
     * a graduate's code is already on the diploma and transcript. The audit log keeps the old and new code.
     *
     * @param id          student id
     * @param studentCode the correct code
     * @return the student after the change
     * @throws com.ums.common.exception.NotFoundException error.student.not_found when it does not exist or was deleted
     * @throws com.ums.common.exception.BusinessException error.student.code_same when nothing changes;
     *                                                    error.student.code_not_current (graduated or dropped);
     *                                                    error.student.code_exists (also codes of deleted students);
     *                                                    error.user.username_taken when another login has it
     */
    StudentResponse changeCode(Long id, String studentCode);

    /**
     * The logged-in user's student records, newest first: the app lets them pick which program to view.
     *
     * @param userId id of the login (users.id), taken from the token
     * @return the student records; empty when the user is not a student
     */
    List<StudentResponse> getByUser(Long userId);

    /// TO DO: not written yet in StudentServiceImpl ------------------

    /**
     * One page of students, deleted ones left out.
     *
     * @param search    filters by student code, first or last name (English or Khmer); null or empty = all
     * @param programId only this program; null = all
     * @param batchId   only this batch; null = all
     * @param yearLevel only this year; null = all
     * @param status    only this status; null = all
     * @param pageable  page number, size and sort (?page=1&size=20&sort=studentCode,asc)
     * @return the students of that page, with the totals
     */
    Page<StudentResponse> list(String search, Long programId, Long batchId, Integer yearLevel,
                               Student.Status status, Pageable pageable);

    /**
     * Changes the student's personal details (in persons) and batch / expected graduation year (in students).
     * The email and phone are also copied to the login (users), so "Forgot password?" uses the new email.
     *
     * @param id      student id
     * @param request the new values
     * @return the student after the change
     * @throws com.ums.common.exception.NotFoundException error.student.not_found when it does not exist or was deleted
     * @throws com.ums.common.exception.BusinessException error.country.unknown for a bad nationality;
     *                                                    error.user.email_taken / error.user.phone_taken when
     *                                                    another login already has them;
     *                                                    error.person.national_id_invalid(_kh) / national_id_taken
     */
    StudentResponse update(Long id, StudentUpdateRequest request);

    /**
     * A real event: SUSPENDED, ON_LEAVE, back to ACTIVE, GRADUATED or DROPPED.
     * Allowed: ACTIVE → any; SUSPENDED / ON_LEAVE → ACTIVE or DROPPED; DROPPED → ACTIVE; GRADUATED → nothing.
     *
     * @param id      student id
     * @param request the new status and the reason (the reason only goes to the log for now)
     * @return the student after the change
     * @throws com.ums.common.exception.NotFoundException error.student.not_found when it does not exist or was deleted
     * @throws com.ums.common.exception.BusinessException error.student.status_same;
     *                                                    error.student.status_not_allowed;
     *                                                    error.student.already_in_program (DROPPED → ACTIVE while
     *                                                    already studying the program again)
     */
    StudentResponse changeStatus(Long id, StudentStatusRequest request);

    /**
     * Soft delete (sets deleted_at), only for a record made by mistake or twice. Its code can't be used again.
     *
     * @param id student id
     * @throws com.ums.common.exception.NotFoundException error.student.not_found when it does not exist or was deleted
     */
    void delete(Long id);

    /**
     * The student's guardians (parents...), primary first. Write it after the Guardian feature.
     *
     * @param id student id
     * @return the guardians; empty when none are linked
     * @throws com.ums.common.exception.NotFoundException error.student.not_found when it does not exist or was deleted
     */
    List<GuardianResponse> getGuardians(Long id);
}
