package com.ums.common.util;

import java.util.Locale;

/**
 * Cleans text that users type, the same way in every module (before: the same helper was copied into each service).
 * <pre>
 * Texts.blankToNull(" Farmer ")  → "Farmer"     optional text: "" or "   " → null
 * Texts.trimToEmpty(null)        → ""           search box: null → "" (= all)
 * Texts.normalizeCode(" it-01 ") → "IT-01"      codes: no spaces, UPPERCASE
 * </pre>
 */
public final class Texts {

    /** Only static methods. */
    private Texts() {
    }

    /**
     * For every String from a request: required (@NotBlank) or optional.
     * The database never stores empty text, and a missing field never crashes (no NullPointerException).
     *
     * @param value the text as the user sent it, may be null
     * @return the text without spaces around it; null when it was null, "" or only spaces
     */
    public static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    /**
     * For the ?search= of a list page. The search queries use "" to mean "everything" (null would break them).
     *
     * @param search the search text, may be null
     * @return "" when null; otherwise the text without spaces around it
     */
    public static String trimToEmpty(String search) {
        return search == null ? "" : search.trim();
    }

    /**
     * For codes (faculty, department, program, building, room ...): saved in ONE form, so " it-01 " and "IT-01"
     * are the same code for the "code exists" check. Locale.ROOT: the same result on every server language.
     *
     * @param code the code from the request (@NotBlank, so never null)
     * @return the code without spaces around it, in UPPERCASE
     */
    public static String normalizeCode(String code) {
        return code.trim().toUpperCase(Locale.ROOT);
    }
}
