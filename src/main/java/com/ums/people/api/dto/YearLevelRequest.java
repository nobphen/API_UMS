package com.ums.people.api.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/** Sets one student's year by hand, e.g. after a credit transfer or a mistake. */
public record YearLevelRequest(@NotNull @Min(1) @Max(10) Integer yearLevel) {
}
