package com.ums.people.domain;

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

/** A province, district, commune or village of Cambodia, with its official code. */
@Getter
@Setter
@Entity
@Table(name = "geo_areas")
public class GeoArea extends BaseEntity {

    /** 2 digits for a province, 4 district, 6 commune, 8 village; starts with the parent's code. */
    @Column(name = "code", nullable = false, unique = true)
    private String code;

    @Enumerated(EnumType.STRING)
    @Column(name = "level", nullable = false)
    private Level level;

    /** Empty only for a province. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private GeoArea parent;

    @Column(name = "name_kh", nullable = false)
    private String nameKh;

    @Column(name = "name_en", nullable = false)
    private String nameEn;

    /** រាជធានី, ខេត្ត, ខណ្ឌ, ក្រុង, ស្រុក, សង្កាត់, ឃុំ, ភូមិ */
    @Column(name = "unit_kh")
    private String unitKh;

    /** Capital, Province, Section, Municipality, District, Quarter, Commune, Village */
    @Column(name = "unit_en")
    private String unitEn;

    /** False when merged or abolished; old addresses still show it, but it can't be chosen for new ones. */
    @Column(name = "is_active", nullable = false)
    private boolean active = true;

    public enum Level {
        PROVINCE, DISTRICT, COMMUNE, VILLAGE;

        /** The level directly above, or null for a province. */
        public Level parentLevel() {
            return this == PROVINCE ? null : values()[ordinal() - 1];
        }
    }
}
