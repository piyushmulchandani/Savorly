package com.application.savorly.mapper;

import com.application.savorly.domain.entity.SavorlyUser;
import com.application.savorly.dto.UserResponse;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface UserMapper {
    UserResponse userToUserResponse(SavorlyUser user);

    List<UserResponse> usersToUserResponses(List<SavorlyUser> users);
}
