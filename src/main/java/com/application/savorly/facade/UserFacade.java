package com.application.savorly.facade;

import com.application.savorly.config.exceptions.BadRequestException;
import com.application.savorly.config.interfaces.hasAdminRole;
import com.application.savorly.config.interfaces.hasAnyRole;
import com.application.savorly.config.interfaces.hasRestaurantAdminRole;
import com.application.savorly.domain.catalog.SavorlyRole;
import com.application.savorly.domain.entity.Restaurant;
import com.application.savorly.domain.entity.SavorlyUser;
import com.application.savorly.dto.modify.UserModificationDto;
import com.application.savorly.dto.response.UserResponseDto;
import com.application.savorly.dto.search.UserSearchDto;
import com.application.savorly.mapper.UserMapper;
import com.application.savorly.service.RestaurantService;
import com.application.savorly.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
public class UserFacade {

    private final UserService userService;
    private final UserMapper userMapper;
    private final RestaurantService restaurantService;
    private final RestaurantFacade restaurantFacade;

    public UserFacade(UserService userService, UserMapper userMapper, RestaurantService restaurantService, RestaurantFacade restaurantFacade) {
        this.userService = userService;
        this.userMapper = userMapper;
        this.restaurantService = restaurantService;
        this.restaurantFacade = restaurantFacade;
    }

    @hasAnyRole
    public void login(String username) {
        try {
            SavorlyUser user = userService.findUserByUsername(username);
            userService.login(user);
        } catch (Exception e) {
            log.info("Registering user: {}", username);
            userService.createUser(UserModificationDto.builder()
                    .username(username)
                    .role(SavorlyRole.USER)
                    .build());
        }
    }

    @hasRestaurantAdminRole
    public void addWorker(String username, Long restaurantId) {
        restaurantFacade.checkRestaurantPermission(restaurantId);
        Restaurant restaurant = restaurantService.getRestaurant(restaurantId);
        SavorlyUser user = userService.findUserByUsername(username);

        if (user.getRestaurant() != null) {
            throw new BadRequestException("The requested user already belongs to a restaurant");
        }

        userService.addRestaurantWorker(user, restaurant);
    }

    @hasAdminRole
    public void deleteUser(String username) {
        SavorlyUser user = userService.findUserByUsername(username);
        userService.deleteUser(user);
    }

    @hasRestaurantAdminRole
    public void removeWorker(String username, Long restaurantId) {
        restaurantFacade.checkRestaurantPermission(restaurantId);
        SavorlyUser user = userService.findUserByUsername(username);

        userService.removeFromRestaurant(user);
    }

    @hasAnyRole
    public UserResponseDto getAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        String username = authentication.getName();
        return userMapper.userToUserResponse(userService.findUserByUsername(username));
    }

    @hasAdminRole
    public List<UserResponseDto> searchUsersBy(UserSearchDto userSearchDto) {
        List<SavorlyUser> users = userService.searchUsersBy(userSearchDto);

        return userMapper.usersToUserResponses(users);
    }

    @hasAdminRole
    public void updateUser(UserModificationDto userModificationDto) {
        if (userModificationDto.getUsername() == null) {
            throw new BadRequestException("Username cannot be empty");
        }

        SavorlyUser user = userService.findUserByUsername(userModificationDto.getUsername());
        userService.updateUser(user, userModificationDto);
    }
}
