package com.ums.common.storage;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ValueSerializer;

/**
 * Writes a file key as its full address. Used through {@link FileUrl}.
 * Jackson creates this class by itself (not through Spring), so it can't get FileStorageService injected:
 * Spring creates one copy at startup (@Component) that keeps the storage in a static field for all copies.
 */
@Component
public class FileUrlSerializer extends ValueSerializer<String> {

    private static FileStorageService storage;

    /** For Jackson: the copies it creates itself. */
    public FileUrlSerializer() {
    }

    /** For Spring at startup: remembers the storage for every copy. */
    @Autowired
    public FileUrlSerializer(FileStorageService fileStorageService) {
        storage = fileStorageService;
    }

    /** key → FileStorageService.url(key); before startup (e.g. a unit test) the key as it is. */
    @Override
    public void serialize(String key, JsonGenerator generator, SerializationContext context) {
        generator.writeString(storage == null ? key : storage.url(key));
    }
}
