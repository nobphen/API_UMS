package com.ums.security.session.application.mapper;

import com.ums.security.session.api.dto.UserSessionResponse;
import com.ums.security.session.domain.UserSession;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserSessionMapper {

    @Mapping(target = "current", expression = "java(session.getId().equals(currentSessionId))")
    UserSessionResponse toResponse(UserSession session, Long currentSessionId);
}
