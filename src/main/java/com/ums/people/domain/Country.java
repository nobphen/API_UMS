package com.ums.people.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/** A country (ISO 3166-1), used for a person's nationality and an address's country. */
@Getter
@Setter
@Entity
@Table(name = "countries")
public class Country {

    public static final String CAMBODIA = "KH";

    /** ISO 3166-1 alpha-2: KH, TH, VN … */
    @Id
    @Column(name = "code")
    private String code;

    @Column(name = "name_en", nullable = false)
    private String nameEn;

    @Column(name = "name_kh", nullable = false)
    private String nameKh;

    /** Cambodian, Thai …; empty for uninhabited territories, which are left out of nationality lists. */
    @Column(name = "nationality_en")
    private String nationalityEn;

    /** ខ្មែរ, ថៃ … */
    @Column(name = "nationality_kh")
    private String nationalityKh;

    @Column(name = "is_active", nullable = false)
    private boolean active = true;
}
