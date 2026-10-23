package com.ums.graduation.api.dto;

import com.ums.graduation.domain.TranscriptRequest;

public record TranscriptRequestResponse(
        Long id,
        String requestNo,
        Long studentId,
        TranscriptRequest.TranscriptType transcriptType,
        int copies,
        Long invoiceId,
        TranscriptRequest.Status status) {
}
