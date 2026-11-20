package com.ums.common.util;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;

/**
 * Dates in the school's time zone (Cambodia, UTC+7), the same way in every module.
 * The server may run in UTC: at 6 AM in Phnom Penh it is still "yesterday" there, so never use
 * LocalDate.now() without a zone. Use these instead:
 * <pre>
 * DateUtils.today()                  → 2026-11-19 (Cambodia)
 * DateUtils.currentYear()            → 2026       (Cambodia)
 * DateUtils.startOfDay(2026-11-19)   → 2026-11-19T00:00+07:00
 * </pre>
 */
public final class DateUtils {

    /** The school's time zone: a "day" starts at 00:00 in Phnom Penh. */
    public static final ZoneId ZONE = ZoneId.of("Asia/Phnom_Penh");

    /** Only static helpers; no objects. */
    private DateUtils() {
    }

    /**
     * Today in Cambodia, whatever the server's time zone is.
     *
     * @return today's date in Asia/Phnom_Penh
     */
    public static LocalDate today() {
        return LocalDate.now(ZONE);
    }

    /**
     * This year in Cambodia (on 31 Dec at 23:00 UTC it is already the next year in Phnom Penh).
     *
     * @return e.g. 2026
     */
    public static int currentYear() {
        return today().getYear();
    }

    /**
     * 00:00 of the day in Cambodia. For "from this day" searches; for "to this day (included)" use
     * startOfDay(day.plusDays(1)) with "&lt;".
     *
     * @param day a date, e.g. 2026-11-19
     * @return 2026-11-19T00:00+07:00 (call .toInstant() when an Instant is needed)
     */
    public static OffsetDateTime startOfDay(LocalDate day) {
        return day.atStartOfDay(ZONE).toOffsetDateTime();
    }
}
