package com.ums.admission.application.domain;

import com.ums.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/** A file attached to an application (ID card, certificates, transcripts, ...). */
@Getter
@Setter
@Entity
@Table(name = "application_documents")
public class ApplicationDocument extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "application_id")
    private Application application;

    @Enumerated(EnumType.STRING)
    @Column(name = "doc_type", nullable = false)
    private DocType docType;

    @Column(name = "file_url", nullable = false)
    private String fileUrl;

    @Column(name = "verified", nullable = false)
    private boolean verified = false;

    /** The staff member who checked the document. */
    @Column(name = "verified_by")
    private Long verifiedBy;

    public enum DocType {
        ID_CARD, PHOTO, BIRTH_CERT, GRADE12_CERT,
        CERTIFICATE, DIPLOMA, ASSOCIATE_DEGREE, BACHELOR_DEGREE, MASTER_DEGREE, TRANSCRIPT,
        CV, RECOMMENDATION, RESEARCH_PROPOSAL, LANGUAGE_CERT, OTHER
    }
}
