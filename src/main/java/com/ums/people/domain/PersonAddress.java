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

/**
 * One address of a person. The village, commune, district and province always belong together:
 * the server fills them from the smallest area chosen.
 */
@Getter
@Setter
@Entity
@Table(name = "person_addresses")
public class PersonAddress extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "person_id")
    private Person person;

    @Enumerated(EnumType.STRING)
    @Column(name = "address_type", nullable = false)
    private Type type;

    @Column(name = "house_no")
    private String houseNo;

    @Column(name = "street")
    private String street;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "village_id")
    private GeoArea village;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "commune_id")
    private GeoArea commune;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "district_id")
    private GeoArea district;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "province_id")
    private GeoArea province;

    @Column(name = "country", nullable = false)
    private String country = "Cambodia";

    /** An address outside Cambodia, or extra detail the lists don't cover. */
    @Column(name = "address_line")
    private String addressLine;

    public enum Type { BIRTH_PLACE, PERMANENT, CURRENT }
}
