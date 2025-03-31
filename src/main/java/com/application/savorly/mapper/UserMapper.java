package com.application.savorly.mapper;

import com.application.savorly.domain.entity.SavorlyUser;
import com.application.savorly.dto.response.UserResponseDto;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring", uses = {RestaurantMapper.class, ReservationMapper.class})
public interface UserMapper {
    UserResponseDto userToUserResponse(SavorlyUser user);

    List<UserResponseDto> usersToUserResponses(List<SavorlyUser> users);
}
