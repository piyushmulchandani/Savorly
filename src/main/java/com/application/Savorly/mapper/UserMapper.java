package com.application.Savorly.mapper;

import com.application.Savorly.domain.entity.SavorlyUser;
import com.application.Savorly.dto.UserResponse;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface UserMapper {
    UserResponse userToUserResponse(SavorlyUser user);

    List<UserResponse> usersToUserResponses(List<SavorlyUser> users);
}
