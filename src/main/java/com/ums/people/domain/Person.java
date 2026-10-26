package com.ums.people.domain;

import com.ums.common.entity.BaseEntity;
import com.ums.common.util.PhoneNumbers;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import java.time.LocalDate;
import lombok.Getter;
import lombok.Setter;
import org.springframework.modulith.NamedInterface;

/** Personal details shared by students, staff and guardians. */
@Getter
@Setter
@Entity
@Table(name = "persons")
@NamedInterface("person")   // used by other modules
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

    /** Always saved as +855969530336, whatever was typed (see {@link #setPhone(String)}). */
    @Column(name = "phone")
    private String phone;

    @Column(name = "email")
    private String email;

    @Column(name = "photo_url")
    private String photoUrl;

    public enum Gender { MALE, FEMALE, OTHER }

    /**
     * "096 953 0336", "0969530336", "+855 96 953 0336" … are all saved as +855969530336, like the login phone,
     * so one number is written one way everywhere. Not a phone number → error.phone.invalid (400).
     */
    public void setPhone(String phone) {
        this.phone = PhoneNumbers.normalizeForSave(phone);
    }
}
