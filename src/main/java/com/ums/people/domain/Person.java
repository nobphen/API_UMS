package com.ums.people.domain;

import com.ums.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import java.time.LocalDate;
import lombok.Getter;
import lombok.Setter;

/** Personal details shared by students, staff and guardians. */
@Getter
@Setter
@Entity
@Table(name = "persons")
public class Person extends BaseEntity {

    @Column(name = "first_name", nullable = false)
    private String firstName;

    @Column(name = "last_name", nullable = false)
    private String lastName;

    @Column(name = "first_name_kh")
    private String firstNameKh;

    @Column(name = "last_name_kh")
    private String lastNameKh;

    @Enumerated(EnumType.STRING)
    @Column(name = "gender")
    private Gender gender;

    @Column(name = "dob")
    private LocalDate dob;

    /** Country code (KH, TH …) of the person's nationality; empty = Cambodian. */
    @Column(name = "nationality_code")
    private String nationalityCode;

    @Column(name = "national_id")
    private String nationalId;

    @Column(name = "phone")
    private String phone;

    @Column(name = "email")
    private String email;

    @Column(name = "photo_url")
    private String photoUrl;

    public enum Gender { MALE, FEMALE, OTHER }
}
