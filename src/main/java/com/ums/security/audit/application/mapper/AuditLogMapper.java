package com.ums.security.audit.application.mapper;

import com.ums.security.audit.api.dto.AuditLogResponse;
import com.ums.security.audit.domain.AuditLog;
import org.mapstruct.Mapper;

/**
 * MapStruct writes the code of this interface at build time (AuditLogMapperImpl in build/generated).
 * AuditLog and AuditLogResponse use the same field names, so every field is copied by itself.
 */
@Mapper(componentModel = "spring")
public interface AuditLogMapper {

    /** One audit row → the response (id, userId, action, entity, entityId, oldValue, newValue, ipAddress, createdAt). */
    AuditLogResponse toResponse(AuditLog log);
}
