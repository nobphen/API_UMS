package com.ums.hostel.api.dto;

import com.ums.hostel.domain.Hostel;

public record HostelResponse(
        Long id,
        String code,
        String name,
        String nameKh,
        Hostel.GenderType genderType,
        String address,
        Long wardenStaffId) {
}
