package com.ums.academic.section.domain;

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

/** One teacher of a class section; a section can have a MAIN lecturer plus ASSISTANT, LAB or TUTORIAL teachers. */
@Getter
@Setter
@Entity
@Table(name = "class_section_lecturers")
public class ClassSectionLecturer extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "class_section_id")
    private ClassSection classSection;

    /** The teacher (people module). */
    @Column(name = "staff_id", nullable = false)
    private Long staffId;

    @Enumerated(EnumType.STRING)
    @Column(name = "teaching_role", nullable = false)
    private TeachingRole teachingRole = TeachingRole.MAIN;

    /** MAIN is responsible for the class and submits the final grades; at most one per section. */
    public enum TeachingRole { MAIN, ASSISTANT, LAB, TUTORIAL }
}
