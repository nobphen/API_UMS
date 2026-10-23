package com.ums.people.api.dto;

import com.ums.people.domain.PersonAddress;

/** One address, with each level for forms and the whole address written out for printing. */
public record AddressResponse(
        Long id,
        PersonAddress.Type type,
        String houseNo,
        String street,
        Area village,
        Area commune,
        Area district,
        Area province,
        String country,
        String addressLine,
        String fullKh,    // ផ្ទះលេខ 12, ផ្លូវ 271, ភូមិ…, សង្កាត់…, ខណ្ឌ…, រាជធានីភ្នំពេញ
        String fullEn) {  // No. 12, St. 271, … Village, … Sangkat, … Khan, Phnom Penh

    public record Area(Long id, String code, String nameKh, String nameEn, String unitKh, String unitEn) {
    }
}
