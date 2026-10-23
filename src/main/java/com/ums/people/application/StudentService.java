package com.ums.people.application;

import com.ums.people.api.dto.NewProgramRequest;
import com.ums.people.api.dto.NewStudentResponse;
import com.ums.people.api.dto.StudentRequest;
import com.ums.people.api.dto.StudentResponse;
import java.util.List;

public interface StudentService {

    /** Adds the student and their login account (username = student code, temporary password). */
    NewStudentResponse create(StudentRequest request);

    /**
     * The person behind an existing student record starts another program (Master after Bachelor, PhD after
     * Master, or a second degree). Same person and login, new student record; no new password.
     */
    NewStudentResponse addProgram(Long studentId, NewProgramRequest request);

    StudentResponse get(Long id);

    /** The logged-in user's student records, newest first: the app lets them pick which program to view. */
    List<StudentResponse> getByUser(Long userId);
}
