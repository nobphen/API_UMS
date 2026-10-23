package com.ums.engagement.alumni.domain;

import com.ums.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/** What a graduate is doing now; one profile per graduated student record. */
@Getter
@Setter
@Entity
@Table(name = "alumni_profiles")
public class AlumniProfile extends BaseEntity {

    /** The graduated student record (people module). */
    @Column(name = "student_id", nullable = false, unique = true)
    private Long studentId;

    @Column(name = "graduation_year", nullable = false)
    private int graduationYear;

    @Column(name = "current_employer")
    private String currentEmployer;

    @Column(name = "job_title")
    private String jobTitle;

    @Column(name = "industry")
    private String industry;

    @Column(name = "linkedin_url")
    private String linkedinUrl;

    /** The graduate agreed to be contacted by the university. */
    @Column(name = "consent_to_contact", nullable = false)
    private boolean consentToContact;
}
