package com.ums.people.application.mapper;

import com.ums.people.api.dto.BatchRequest;
import com.ums.people.api.dto.BatchResponse;
import com.ums.people.domain.Batch;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

/**
 * Batch request → entity → response (MapStruct writes the code at build time).
 * Fields with the same name are copied on their own: programId, name, nameKh, startYear.
 * id, deletedAt, createdAt and updatedAt are never taken from a request: the database and the service set them.
 */
@Mapper(componentModel = "spring")
public interface BatchMapper {

    /** Entity → what the client gets back: id, programId, name, nameKh, startYear. */
    BatchResponse toResponse(Batch batch);

    /** Create form → a new entity, not saved yet. The service trims the names after this. */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Batch toEntity(BatchRequest request);

    /** Update form → the batch already loaded (@MappingTarget: no new object). The service trims the names after this. */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void update(BatchRequest request, @MappingTarget Batch batch);
}
