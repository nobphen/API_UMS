package com.ums.common.util;

import com.ums.common.exception.BusinessException;
import java.util.regex.Pattern;

/**
 * Phone numbers in one format (+85512345678), so "012 345 678", "+855 12 345 678" and "85512345678" are the same
 * number when saved and when someone logs in with it. Numbers without a country code are taken as Cambodian.
 */
public final class PhoneNumbers {

    private static final String CAMBODIA = "855";
    private static final Pattern E164 = Pattern.compile("^\\+[1-9]\\d{7,14}$");
    private static final Pattern SEPARATORS = Pattern.compile("[\\s\\-.()]");

    private PhoneNumbers() {
    }

    /**
     * The number in +85512345678 form, or null when the text is empty or is not a phone number.
     * <pre>
     * "012 345 678"     → "+85512345678"
     * "+855 12-345-678" → "+85512345678"
     * "85512345678"     → "+85512345678"
     * "00855 12345678"  → "+85512345678"
     * "+66 81 234 5678" → "+66812345678"
     * "admin"           → null
     * </pre>
     *
     * @param text phone number as typed
     * @return the normalized number, or null
     */
    public static String normalize(String text) {
        if (text == null) {
            return null;
        }
        String value = SEPARATORS.matcher(text.trim()).replaceAll("");
        if (value.startsWith("00")) {
            value = "+" + value.substring(2);
        } else if (value.startsWith("0")) {
            value = "+" + CAMBODIA + value.substring(1);
        } else if (value.startsWith(CAMBODIA)) {
            value = "+" + value;
        }
        return E164.matcher(value).matches() ? value : null;
    }

    /**
     * For saving a phone number: like {@link #normalize(String)}, but text that is not a phone number is an error.
     *
     * @param text phone number as typed; null or blank means no phone
     * @return the normalized number, or null when no phone was given
     * @throws BusinessException error.phone.invalid when the text is not a phone number
     */
    public static String normalizeForSave(String text) {
        if (text == null || text.isBlank()) {
            return null;
        }
        String phone = normalize(text);
        if (phone == null) {
            throw new BusinessException("error.phone.invalid", text);
        }
        return phone;
    }

    /**
     * For showing a saved number (API answers): Cambodian numbers in the local form people read, others unchanged.
     * <pre>
     * "+855969530336" → "096 953 0336"
     * "+85512345678"  → "012 345 678"
     * "+66812345678"  → "+66812345678"
     * </pre>
     * The answer can be sent back as it is: {@link #normalize(String)} turns it into +855… again.
     *
     * @param phone saved number, e.g. +855969530336
     * @return the number to show, or the text unchanged when it is not a Cambodian number
     */
    public static String format(String phone) {
        String normalized = normalize(phone);
        if (normalized == null || !normalized.startsWith("+" + CAMBODIA)) {
            return phone;
        }
        String local = "0" + normalized.substring(CAMBODIA.length() + 1);
        if (local.length() != 9 && local.length() != 10) {
            return local;
        }
        return local.substring(0, 3) + " " + local.substring(3, 6) + " " + local.substring(6);
    }
}
