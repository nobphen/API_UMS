package com.ums.common.storage;

import com.fasterxml.jackson.annotation.JacksonAnnotationsInside;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import tools.jackson.databind.annotation.JsonSerialize;

/**
 * Put on a file field of a response DTO (like @PhoneDisplay): the saved key persons/7/0c9f….jpg is sent as
 * a signed link http://localhost:9000/ums/persons/7/0c9f….jpg?X-Amz-Signature=… valid 15 min (FileStorageService.url).
 * Only the JSON answer changes; the database keeps the key. Each GET gives a fresh link.
 */
@Target({ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER, ElementType.RECORD_COMPONENT})
@Retention(RetentionPolicy.RUNTIME)
@JacksonAnnotationsInside
@JsonSerialize(using = FileUrlSerializer.class)
public @interface FileUrl {
}
