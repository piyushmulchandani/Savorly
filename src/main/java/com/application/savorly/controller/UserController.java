package com.application.savorly.controller;

import com.application.savorly.dto.UserDto;
import com.application.savorly.dto.UserResponse;
import com.application.savorly.dto.UserSearchParamsDto;
import com.application.savorly.facade.UserFacade;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserFacade userFacade;

    public UserController(UserFacade userFacade) {
        this.userFacade = userFacade;
    }

    @PostMapping("/login/{username}")
    public void registerUser(
            @PathVariable String username
    ) {
        log.info("Registering user: {}", username);
        userFacade.login(username);
    }

    @DeleteMapping("/delete/{username}")
    public void deleteUser(
            @PathVariable String username
    ) {
        log.info("Deleting user: {}", username);
        userFacade.deleteUser(username);
    }

    @GetMapping("/me")
    public UserResponse getCurrentUser() {
        return userFacade.getAuthenticatedUser();
    }

    @GetMapping("")
    public List<UserResponse> getAllUsers(
            @ParameterObject @Valid UserSearchParamsDto userSearchParamsDto
    ) {
        return userFacade.searchUsersBy(userSearchParamsDto);
    }

    @PatchMapping("")
    public void updateUser(
            @RequestBody UserDto userDto
    ) {
        userFacade.updateUser(userDto);
    }
}
