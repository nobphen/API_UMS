package com.ums.lms.domain;

import com.ums.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import lombok.Getter;
import lombok.Setter;

/** A file, link, video or note a lecturer shares with a class section. */
@Getter
@Setter
@Entity
@Table(name = "course_materials")
public class CourseMaterial extends BaseEntity {

    @Column(name = "class_section_id", nullable = false)
    private Long classSectionId;

    @Column(name = "week_no")
    private Integer weekNo;

    @Column(name = "title", nullable = false)
    private String title;

    @Enumerated(EnumType.STRING)
    @Column(name = "material_type", nullable = false)
    private MaterialType materialType;

    @Column(name = "file_url")
    private String fileUrl;

    @Column(name = "link_url")
    private String linkUrl;

    @Column(name = "content")
    private String content;

    /** Students see the material from this moment; empty = draft. */
    @Column(name = "published_at")
    private OffsetDateTime publishedAt;

    public enum MaterialType { FILE, LINK, VIDEO, NOTE }
    /** The lecturer (staff id) who posted it. */
    @Column(name = "created_by")
    private Long createdBy;

    /** Filled by the database when the row is created. */
    @Column(name = "created_at", insertable = false, updatable = false)
    private OffsetDateTime createdAt;
}
