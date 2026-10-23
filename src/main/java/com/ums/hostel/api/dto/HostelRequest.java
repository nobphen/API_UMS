package com.ums.hostel.api.dto;

import com.ums.hostel.domain.Hostel;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record HostelRequest(
        @NotBlank @Size(max = 20) String code,
        @NotBlank @Size(max = 100) String name,
        @Size(max = 100) String nameKh,
        @NotNull Hostel.GenderType genderType,
        String address,
        Long wardenStaffId) {
}
