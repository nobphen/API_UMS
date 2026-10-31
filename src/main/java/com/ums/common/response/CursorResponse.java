package com.ums.common.response;

import java.util.List;
import java.util.function.Function;

/**
 * A slice of records loaded from an id (a cursor) instead of a page number, the {@code data} of
 * {@link ApiResponse#cursor(CursorResponse)}. Used for chat messages, where new rows keep arriving:
 * <pre>
 * ?limit=30                  the latest 30
 * ?before_id=500&limit=30    30 older than 500 (scroll up)
 * ?after_id=265&limit=30     30 newer than 265 (scroll down)
 * ?around_id=250&limit=30    15 before 250, 250 itself, 15 after (tap a reply)
 * </pre>
 * {@code items} are always old → new. The next request sends {@code oldestId} as before_id when
 * {@code hasMoreBefore}, or {@code newestId} as after_id when {@code hasMoreAfter}. No total count: counting
 * all messages is slow and the screen does not need it (load limit + 1 rows to know if there are more).
 */
public record CursorResponse<T>(
        List<T> items,
        boolean hasMoreBefore,
        boolean hasMoreAfter,
        Long oldestId,
        Long newestId) {

    /** items must be old → new; idOf reads the id of one item (e.g. MessageResponse::id). Empty list → null ids. */
    public static <T> CursorResponse<T> of(List<T> items, boolean hasMoreBefore, boolean hasMoreAfter,
                                           Function<T, Long> idOf) {
        Long oldestId = items.isEmpty() ? null : idOf.apply(items.getFirst());
        Long newestId = items.isEmpty() ? null : idOf.apply(items.getLast());
        return new CursorResponse<>(items, hasMoreBefore, hasMoreAfter, oldestId, newestId);
    }
}
