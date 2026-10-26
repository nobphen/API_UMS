package com.ums.common.response;

import com.fasterxml.jackson.annotation.JacksonAnnotationsInside;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import tools.jackson.databind.annotation.JsonSerialize;

/**
 * Put on a phone field of a response DTO: the saved +855969530336 is sent as 096 953 0336
 * (see PhoneNumbers.format). Only the JSON answer changes; the database keeps +855….
 */
@Target({ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER, ElementType.RECORD_COMPONENT})
@Retention(RetentionPolicy.RUNTIME)
@JacksonAnnotationsInside
@JsonSerialize(using = PhoneDisplaySerializer.class)
public @interface PhoneDisplay {
}
