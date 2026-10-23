package com.ums.graduation.domain;

import com.ums.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import java.time.LocalDate;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "certificates")
public class Certificate extends BaseEntity {

    @Column(name = "student_id", nullable = false)
    private Long studentId;

    @Enumerated(EnumType.STRING)
    @Column(name = "certificate_type", nullable = false)
    private CertificateType certificateType;

    @Column(name = "certificate_no", nullable = false, unique = true)
    private String certificateNo;

    @Column(name = "issue_date", nullable = false)
    private LocalDate issueDate;

    /** Lets anyone check that a certificate is genuine. */
    @Column(name = "verification_code", nullable = false, unique = true)
    private String verificationCode;

    @Column(name = "file_url")
    private String fileUrl;

    public enum CertificateType { DEGREE, ENROLLMENT, COMPLETION }
}
