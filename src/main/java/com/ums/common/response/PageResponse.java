package com.ums.common.response;

import java.util.List;
import org.springframework.data.domain.Page;

/**
 * One page of records, the {@code data} of {@link ApiResponse#page(Page)}.
 * {@code page} starts at 1, like the request: ?page=1&size=20 is the first page
 * (spring.data.web.pageable.one-indexed-parameters in application.yml; Spring counts from 0 inside).
 */
public record PageResponse<T>(
        List<T> content,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean first,
        boolean last) {

    public static <T> PageResponse<T> of(Page<T> page) {
        return new PageResponse<>(page.getContent(), page.getNumber() + 1, page.getSize(),
                page.getTotalElements(), page.getTotalPages(), page.isFirst(), page.isLast());
    }
}
