package com.ums.graduation.api.dto;

import com.ums.graduation.domain.Certificate;
import java.time.LocalDate;

public record CertificateResponse(
        Long id,
        Long studentId,
        Certificate.CertificateType certificateType,
        String certificateNo,
        LocalDate issueDate,
        String verificationCode,
        String fileUrl) {
}
