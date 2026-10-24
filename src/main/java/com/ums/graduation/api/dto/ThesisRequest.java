package com.ums.graduation.api.dto;

import com.ums.graduation.domain.Thesis;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** A thesis proposal. Status, dates, defense and result are set by the steps that follow, not sent here. */
public record ThesisRequest(
        @NotNull Long studentId,
        @NotNull Thesis.ThesisType thesisType,
        @NotBlank @Size(max = 300) String title,
        @Size(max = 300) String titleKh,
        String summary,
        String summaryKh) {
}
