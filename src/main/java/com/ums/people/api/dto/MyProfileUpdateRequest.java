package com.ums.people.api.dto;

import com.ums.common.util.Emails;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

/**
 * What a logged-in person may change by themselves: contact and current address.
 * Names, birth date, gender, nationality and national ID are official data: only an admin changes them.
 * Used by PUT /persons/me.
 * <pre>{"phone": "016777555", "email": "dara@gmail.com",
 *  "currentAddress": {"type": "CURRENT", "areaId": 9098, "houseNo": "12A", "street": "St 271"}}</pre>
 * areaId = geo_areas.id (9098), not the official code (12010101).
 */
public record MyProfileUpdateRequest(
        @Size(max = 30) String phone,                                                                    // empty = no phone
        @Email(regexp = Emails.REGEX, message = "{validation.email}") @Size(max = 150) String email,    // empty = no email
        @Valid AddressRequest currentAddress) {   // type must be CURRENT; null = keep the current address as it is
}
