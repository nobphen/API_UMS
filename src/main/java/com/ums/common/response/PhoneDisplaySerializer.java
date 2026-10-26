package com.ums.common.response;

import com.ums.common.util.PhoneNumbers;
import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ValueSerializer;

/** Writes a phone number for people to read: +855969530336 → 096 953 0336. Used through {@link PhoneDisplay}. */
public class PhoneDisplaySerializer extends ValueSerializer<String> {

    @Override
    public void serialize(String value, JsonGenerator generator, SerializationContext context) {
        generator.writeString(PhoneNumbers.format(value));
    }
}
