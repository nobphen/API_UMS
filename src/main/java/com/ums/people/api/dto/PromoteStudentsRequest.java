package com.ums.people.api.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.util.List;

/**
 * End of the academic year: every ACTIVE student of the program (and batch) in fromYear moves to fromYear + 1,
 * except the ones listed in keepStudentIds (repeating the year).
 */
public record PromoteStudentsRequest(
        @NotNull Long programId,
        Long batchId,                  // empty = every batch of the program
        @NotNull @Min(1) @Max(9) Integer fromYear,
        List<Long> keepStudentIds) {   // students repeating the year
}
