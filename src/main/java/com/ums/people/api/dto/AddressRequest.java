package com.ums.people.api.dto;

import com.ums.people.domain.PersonAddress;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * One address. In Cambodia send areaId, the smallest area chosen (usually the village); the server fills the
 * commune, district and province. Outside Cambodia send the country and addressLine instead.
 */
public record AddressRequest(
        @NotNull PersonAddress.Type type,
        Long areaId,
        @Size(max = 30) String houseNo,
        @Size(max = 100) String street,
        @Size(max = 60) String country,   // empty = Cambodia
        String addressLine) {
}
