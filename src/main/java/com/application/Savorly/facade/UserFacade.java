package com.application.Savorly.facade;

import com.application.Savorly.config.exceptions.BadRequestException;
import com.application.Savorly.config.exceptions.UnauthorizedException;
import com.application.Savorly.config.interfaces.hasAdminRole;
import com.application.Savorly.config.interfaces.hasAnyRole;
import com.application.Savorly.domain.catalog.SavorlyRole;
import com.application.Savorly.domain.entity.SavorlyUser;
import com.application.Savorly.dto.UserDto;
import com.application.Savorly.dto.UserResponse;
import com.application.Savorly.dto.UserSearchParamsDto;
import com.application.Savorly.mapper.UserMapper;
import com.application.Savorly.service.UserService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class UserFacade {

    private final UserService userService;
    private final UserMapper userMapper;

    public UserFacade(UserService userService, UserMapper userMapper) {
        this.userService = userService;
        this.userMapper = userMapper;
    }

    @hasAnyRole
    public void login(String username) {
        try {
            SavorlyUser user = userService.findUserByUsername(username);
            userService.login(user);
        } catch (Exception e) {
            userService.createUser(UserDto.builder()
                    .username(username)
                    .role(SavorlyRole.USER)
                    .build());
        }
    }

    @hasAdminRole
    public void deleteUser(String username) {
        SavorlyUser user = userService.findUserByUsername(username);
        userService.deleteUser(user);
    }

    @hasAnyRole
    public UserResponse getAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || authentication.getName() == null) {
            throw new UnauthorizedException("Error getting authorization");
        }

        String username = authentication.getName();
        return userMapper.userToUserResponse(userService.findUserByUsername(username));
    }

    @hasAdminRole
    public List<UserResponse> searchUsersBy(UserSearchParamsDto userSearchParamsDto) {
        List <SavorlyUser> users = userService.searchUsersBy(userSearchParamsDto);

        return userMapper.usersToUserResponses(users);
    }

    @hasAnyRole
    public void updateUser(UserDto userDto) {
        if(userDto.getUsername() == null){
            throw new BadRequestException("Username cannot be empty");
        }

        SavorlyUser user = userService.findUserByUsername(userDto.getUsername());
        userService.updateUser(user, userDto);
    }
}
