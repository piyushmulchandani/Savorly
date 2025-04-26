package com.application.savorly.controller;

import com.application.savorly.dto.modify.UserModificationDto;
import com.application.savorly.dto.response.UserResponseDto;
import com.application.savorly.dto.search.UserSearchDto;
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
        userFacade.login(username);
    }

    @PostMapping("/add-worker/{username}")
    public void addWorker(
            @PathVariable String username,
            @RequestParam Long restaurantId
    ) {
        userFacade.addWorker(username, restaurantId);
    }

    @DeleteMapping("/delete/{username}")
    public void deleteUser(
            @PathVariable String username
    ) {
        log.info("Deleting user: {}", username);
        userFacade.deleteUser(username);
    }

    @DeleteMapping("/remove-worker/{username}")
    public void removeWorker(
            @PathVariable String username,
            @RequestParam Long restaurantId
    ) {
        userFacade.removeWorker(username, restaurantId);
    }

    @GetMapping("/me")
    public UserResponseDto getCurrentUser() {
        return userFacade.getAuthenticatedUser();
    }

    @GetMapping
    public List<UserResponseDto> getAllUsers(
            @ParameterObject @Valid UserSearchDto userSearchDto
    ) {
        return userFacade.searchUsersBy(userSearchDto);
    }

    @PatchMapping
    public void updateUser(
            @RequestBody UserModificationDto userDto
    ) {
        userFacade.updateUser(userDto);
    }
}
