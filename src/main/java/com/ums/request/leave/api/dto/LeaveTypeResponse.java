package com.ums.request.leave.api.dto;

public record LeaveTypeResponse(
        Long id,
        String code,
        String name,
        String nameKh,
        boolean requiresDocument,
        Integer maxDays) {
}
