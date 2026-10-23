package com.ums.graduation.api.dto;

import com.ums.graduation.domain.Certificate;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

/** The certificate number and verification code are generated. */
public record CertificateRequest(
        @NotNull Long studentId,
        @NotNull Certificate.CertificateType certificateType,
        @NotNull LocalDate issueDate,
        @Size(max = 500) String fileUrl) {
}
