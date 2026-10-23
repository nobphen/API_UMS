package com.ums.graduation.api.dto;

import com.ums.graduation.domain.TranscriptRequest;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/** The request number is generated; the fee invoice is made by the server. */
public record TranscriptRequestRequest(
        @NotNull Long studentId,
        @NotNull TranscriptRequest.TranscriptType transcriptType,
        @Positive Integer copies) {   // empty = 1
}
