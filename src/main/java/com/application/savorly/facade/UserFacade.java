package com.application.savorly.facade;

import com.application.savorly.config.exceptions.BadRequestException;
import com.application.savorly.config.interfaces.hasAdminRole;
import com.application.savorly.config.interfaces.hasAnyRole;
import com.application.savorly.domain.catalog.SavorlyRole;
import com.application.savorly.domain.entity.SavorlyUser;
import com.application.savorly.dto.UserDto;
import com.application.savorly.dto.UserResponse;
import com.application.savorly.dto.UserSearchParamsDto;
import com.application.savorly.mapper.UserMapper;
import com.application.savorly.service.UserService;
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

        String username = authentication.getName();
        return userMapper.userToUserResponse(userService.findUserByUsername(username));
    }

    @hasAdminRole
    public List<UserResponse> searchUsersBy(UserSearchParamsDto userSearchParamsDto) {
        List<SavorlyUser> users = userService.searchUsersBy(userSearchParamsDto);

        return userMapper.usersToUserResponses(users);
    }

    @hasAnyRole
    public void updateUser(UserDto userDto) {
        if (userDto.getUsername() == null) {
            throw new BadRequestException("Username cannot be empty");
        }

        SavorlyUser user = userService.findUserByUsername(userDto.getUsername());
        userService.updateUser(user, userDto);
    }
}
