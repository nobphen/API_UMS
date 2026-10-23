package com.ums.people.domain;

import com.ums.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/** An intake of one program, e.g. "Batch 12"; the name is unique within the program. */
@Getter
@Setter
@Entity
@Table(name = "batches")
public class Batch extends BaseEntity {

    /** The program (academic module). */
    @Column(name = "program_id", nullable = false)
    private Long programId;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "start_year", nullable = false)
    private int startYear;
}
