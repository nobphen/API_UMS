package com.ums.finance.fee.api.dto;

public record FeeTypeResponse(
        Long id,
        String code,
        String name,
        String nameKh,
        boolean refundable) {
}
