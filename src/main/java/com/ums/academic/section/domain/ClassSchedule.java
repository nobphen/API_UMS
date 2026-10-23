package com.ums.academic.section.domain;

import com.ums.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalTime;
import lombok.Getter;
import lombok.Setter;

/** A weekly time slot of a class section. */
@Getter
@Setter
@Entity
@Table(name = "class_schedules")
public class ClassSchedule extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "class_section_id")
    private ClassSection classSection;

    /** Who teaches this slot, e.g. the lab teacher (staff id, people module); null = the MAIN lecturer. */
    @Column(name = "lecturer_id")
    private Long lecturerId;

    /** The room (organization module). */
    @Column(name = "room_id")
    private Long roomId;

    /** 1 = Monday ... 7 = Sunday. */
    @Column(name = "day_of_week", nullable = false)
    private int dayOfWeek;

    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalTime endTime;
}
