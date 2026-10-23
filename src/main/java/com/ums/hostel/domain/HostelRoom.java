package com.ums.hostel.domain;

import com.ums.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "hostel_rooms")
public class HostelRoom extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "hostel_id")
    private Hostel hostel;

    /** Unique within the hostel. */
    @Column(name = "room_no", nullable = false)
    private String roomNo;

    @Column(name = "floor")
    private Integer floor;

    @Enumerated(EnumType.STRING)
    @Column(name = "room_type", nullable = false)
    private RoomType roomType;

    /** Number of beds. */
    @Column(name = "capacity", nullable = false)
    private int capacity;

    @Column(name = "price_per_semester", nullable = false)
    private BigDecimal pricePerSemester;

    @Column(name = "status", nullable = false)
    private String status = "ACTIVE";

    public enum RoomType { SINGLE, DOUBLE, QUAD }
}
