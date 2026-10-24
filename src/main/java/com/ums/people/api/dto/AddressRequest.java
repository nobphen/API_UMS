package com.ums.people.api.dto;

import com.ums.people.domain.PersonAddress;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.springframework.modulith.NamedInterface;

/**
 * One address. In Cambodia (countryCode KH, the default) send areaId, the smallest area chosen (usually the
 * village); the server fills the commune, district and province. Abroad send countryCode and addressLine instead.
 */
@NamedInterface("address-request")   // used by other modules
public record AddressRequest(
        @NotNull PersonAddress.Type type,
        @Pattern(regexp = "^[A-Z]{2}$", message = "{validation.country_code}")
        String countryCode,   // empty = KH (Cambodia)
        Long areaId,
        @Size(max = 30) String houseNo,
        @Size(max = 100) String street,
        String addressLine) {
}
