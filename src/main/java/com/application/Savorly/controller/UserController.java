package com.application.Savorly.controller;

import com.application.Savorly.dto.UserDto;
import com.application.Savorly.dto.UserResponse;
import com.application.Savorly.dto.UserSearchParamsDto;
import com.application.Savorly.facade.UserFacade;
import jakarta.validation.Valid;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
        System.out.println("Registering user: " + username);
        userFacade.login(username);
    }

    @DeleteMapping("/delete/{username}")
    public void deleteUser(
            @PathVariable String username
    ) {
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
