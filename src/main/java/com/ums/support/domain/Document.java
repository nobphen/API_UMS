package com.ums.support.domain;

import com.ums.common.audit.Auditable;
import com.ums.common.entity.SoftDeletableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/** A file attached to any record, found by ownerType and ownerId (e.g. STUDENT and the student id). */
@Getter
@Setter
@Auditable
@Entity
@Table(name = "documents")
public class Document extends SoftDeletableEntity {

    @Column(name = "owner_type", nullable = false)
    private String ownerType;

    @Column(name = "owner_id", nullable = false)
    private Long ownerId;

    @Column(name = "doc_type")
    private String docType;

    @Column(name = "file_name", nullable = false)
    private String fileName;

    @Column(name = "file_url", nullable = false)
    private String fileUrl;

    @Column(name = "mime_type")
    private String mimeType;

    /** The login account (security module) that uploaded the file. */
    @Column(name = "uploaded_by")
    private Long uploadedBy;
}
