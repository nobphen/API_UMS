package com.ums.common.util;

/**
 * One email rule for every form. The default {@code @Email} also accepts "dara.sok@gmail" (no dot after @),
 * because "admin@localhost" is allowed by the email standard; a real student or staff email always has a domain
 * ending such as .com or .edu.kh. Use it on every email field:
 * <pre>
 * &#64;Email(regexp = Emails.REGEX, message = "{validation.email}") String email
 * </pre>
 */
public final class Emails {

    /**
     * name@domain.end: no spaces, exactly one @, and a dot then at least 2 letters at the end.
     * dara.sok@gmail.com ✓, dara@rupp.edu.kh ✓, dara.sok@gmail ✗, dara sok@gmail.com ✗.
     * Empty "" passes: email is optional on most forms (saved as null); add @NotBlank where it is required.
     */
    public static final String REGEX = "^$|^[^@\\s]+@[^@\\s]+\\.[A-Za-z]{2,}$";

    /** Only the constant; no objects. */
    private Emails() {
    }
}
