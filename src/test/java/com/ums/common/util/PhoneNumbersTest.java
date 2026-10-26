package com.ums.common.util;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.ums.common.exception.BusinessException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

class PhoneNumbersTest {

    @ParameterizedTest
    @CsvSource({
            "012345678,          +85512345678",
            "012 345 678,        +85512345678",
            "012-345-678,        +85512345678",
            "(012) 345.678,      +85512345678",
            "+855 12 345 678,    +85512345678",
            "85512345678,        +85512345678",
            "0085512345678,      +85512345678",
            "097 7777 777,       +855977777777",
            "+66 81 234 5678,    +66812345678"
    })
    void normalizesToOneFormat(String typed, String expected) {
        assertThat(PhoneNumbers.normalize(typed)).isEqualTo(expected);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"admin", "admin@ums.local", "ST2026001", "12345", "+0123456789", "0123456789012345678"})
    void returnsNullForTextThatIsNotAPhone(String typed) {
        assertThat(PhoneNumbers.normalize(typed)).isNull();
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   "})
    void noPhoneIsAllowedWhenSaving(String typed) {
        assertThat(PhoneNumbers.normalizeForSave(typed)).isNull();
    }

    @ParameterizedTest
    @ValueSource(strings = {"admin", "12345"})
    void wrongPhoneIsAnErrorWhenSaving(String typed) {
        assertThatThrownBy(() -> PhoneNumbers.normalizeForSave(typed))
                .isInstanceOf(BusinessException.class)
                .hasMessage("error.phone.invalid");
    }

    @ParameterizedTest
    @CsvSource({
            "+855969530336,  096 953 0336",
            "+85512345678,   012 345 678",
            "+66812345678,   +66812345678",
            "096 953 0336,   096 953 0336",
            "admin,          admin"
    })
    void formatsForPeopleToRead(String saved, String shown) {
        assertThat(PhoneNumbers.format(saved)).isEqualTo(shown);
    }

    @ParameterizedTest
    @ValueSource(strings = {"+855969530336", "+85512345678", "+66812345678"})
    void shownNumberIsSavedTheSameWayAgain(String saved) {
        assertThat(PhoneNumbers.normalize(PhoneNumbers.format(saved))).isEqualTo(saved);
    }

    @Test
    void noPhoneShowsNothing() {
        assertThat(PhoneNumbers.format(null)).isNull();
    }
}
