package com.ums.people.api.dto;

/** How many students moved up a year, and how many stayed because they repeat it. */
public record PromoteStudentsResponse(int promoted, int kept) {
}
