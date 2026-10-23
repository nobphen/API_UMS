package com.ums.enrollment.grading.api.dto;

import com.ums.enrollment.grading.domain.StudentSemesterResult;
import java.math.BigDecimal;

public record StudentSemesterResultResponse(
        Long id,
        Long studentId,
        Long semesterId,
        int creditsAttempted,
        int creditsEarned,
        BigDecimal gpa,
        BigDecimal cgpa,
        StudentSemesterResult.AcademicStanding academicStanding) {
}
