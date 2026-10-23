package com.ums.admission.application.domain;

import com.ums.admission.formsale.domain.ApplicationFormSale;
import com.ums.admission.intake.domain.AdmissionIntake;
import com.ums.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "applications")
public class Application extends BaseEntity {

    @Column(name = "application_no", nullable = false, unique = true)
    private String applicationNo;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "applicant_id")
    private Applicant applicant;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "intake_id")
    private AdmissionIntake intake;

    /** The paper form bought for this application; one form serves one application. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "form_sale_id", unique = true)
    private ApplicationFormSale formSale;

    @Column(name = "first_choice_program_id", nullable = false)
    private Long firstChoiceProgramId;

    @Column(name = "second_choice_program_id")
    private Long secondChoiceProgramId;

    /** The program the applicant was finally admitted to. */
    @Column(name = "admitted_program_id")
    private Long admittedProgramId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private Status status = Status.SUBMITTED;

    /** The last study done before this application (the applicant keeps the grade 12 details). */
    @Enumerated(EnumType.STRING)
    @Column(name = "previous_level")
    private PreviousLevel previousLevel;

    @Column(name = "previous_institution")
    private String previousInstitution;

    @Column(name = "previous_major")
    private String previousMajor;

    @Column(name = "previous_year")
    private Integer previousYear;

    /** GPA, grade or mention, as written on the certificate. */
    @Column(name = "previous_result")
    private String previousResult;

    /** Set when they studied here before (people module). */
    @Column(name = "previous_student_id")
    private Long previousStudentId;

    @Column(name = "submitted_at", nullable = false)
    private OffsetDateTime submittedAt = OffsetDateTime.now();

    /** The staff member who decided. */
    @Column(name = "decided_by")
    private Long decidedBy;

    @Column(name = "decided_at")
    private OffsetDateTime decidedAt;

    public enum Status { SUBMITTED, REVIEWING, EXAM, ACCEPTED, WAITLISTED, REJECTED, ENROLLED, CANCELLED }

    public enum PreviousLevel { HIGH_SCHOOL, FOUNDATION, CERTIFICATE, DIPLOMA, ASSOCIATE, BACHELOR, MASTER, PHD }
}
