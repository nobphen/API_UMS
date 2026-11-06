package com.ums.security.session.application.mapper;

import com.ums.security.session.api.dto.UserSessionResponse;
import com.ums.security.session.domain.UserSession;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/** MapStruct: UserSession entity → UserSessionResponse (one row on the "Devices" screen). */
@Mapper(componentModel = "spring")
public interface UserSessionMapper {

    /**
     * Copies the device fields (token hash, FCM token and user agent are not in the response);
     * current = true when this is the session making the request. Pass null for currentSessionId to mark none.
     */
    @Mapping(target = "current", expression = "java(session.getId().equals(currentSessionId))")
    UserSessionResponse toResponse(UserSession session, Long currentSessionId);
}
