package com.application.Savorly.controller;

import com.application.Savorly.SavorlyApplication;
import com.application.Savorly.config.interfaces.WithMockCustomUser;
import com.application.Savorly.domain.catalog.SavorlyRole;
import com.application.Savorly.domain.entity.SavorlyUser;
import com.application.Savorly.dto.UserDto;
import com.application.Savorly.dto.UserResponse;
import com.application.Savorly.repository.UserRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.TestInstance;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.http.MediaType;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
@ActiveProfiles("test")
@SpringBootTest(classes = {SavorlyApplication.class})
@AutoConfigureMockMvc
@TestPropertySource(locations = "classpath:/application-test.properties")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Transactional
class UserControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @MockitoBean
    private Keycloak keycloak;

    @Test
    @WithMockCustomUser(username = "newUser")
    void shouldRegisterUser() throws Exception {
        String username = "newUser";

        mockMvc.perform(post("/api/v1/users/login/{username}", username))
                .andExpect(status().isOk());

        assertTrue(userRepository.findByUsernameIgnoreCase(username).isPresent());
    }

    @Test
    @WithMockCustomUser(username = "newUser")
    void shouldLoginUser() throws Exception {
        SavorlyUser user = SavorlyUser.builder()
                .username("loginUser")
                .role(SavorlyRole.USER)
                .build();
        userRepository.save(user);

        mockMvc.perform(post("/api/v1/users/login/{username}", user.getUsername()))
                .andExpect(status().isOk());

        assertTrue(userRepository.findByUsernameIgnoreCase(user.getUsername()).isPresent());
        assertThat(userRepository.findByUsernameIgnoreCase(user.getUsername()).get().getLastLogonDate()).isNotNull();
    }

    @Test
    @WithMockCustomUser(role = "admin")
    void shouldDeleteUser() throws Exception {
        SavorlyUser user = SavorlyUser.builder()
                .username("deleteUser")
                .role(SavorlyRole.USER)
                .build();
        userRepository.save(user);

        UsersResource mockResource = mock(UsersResource.class);
        RealmResource mockRealm = mock(RealmResource.class);
        when(keycloak.realm(any())).thenReturn(mockRealm);
        when(mockRealm.users()).thenReturn(mockResource);
        when(mockResource.search(any())).thenReturn(List.of());

        mockMvc.perform(delete("/api/v1/users/delete/{username}", "deleteUser"))
                .andExpect(status().isOk());

        assertFalse(userRepository.findByUsernameIgnoreCase("deleteUser").isPresent());
    }

    @Test
    @WithMockCustomUser
    void deleteUser_isForbidden() throws Exception {
        mockMvc.perform(delete("/api/v1/users/delete/{username}", "deleteUser"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockCustomUser(username = "testUser")
    void shouldGetCurrentUser() throws Exception {
        SavorlyUser user = SavorlyUser.builder()
                .username("testUser")
                .role(SavorlyRole.USER)
                .build();
        userRepository.save(user);

        MvcResult actual = mockMvc.perform(get("/api/v1/users/me"))
                .andExpect(status().isOk())
                .andReturn();

        UserResponse response = objectMapper.readValue(actual.getResponse().getContentAsString(), UserResponse.class);
        assertEquals("testUser", response.getUsername());
        assertEquals(SavorlyRole.USER, response.getRole());
    }

    @Test
    @WithMockCustomUser
    void getCurrentUser_isNotFound() throws Exception {
        mockMvc.perform(get("/api/v1/users/me"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getCurrentUser_isUnauthorized() throws Exception {
        mockMvc.perform(get("/api/v1/users/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockCustomUser(role = "admin")
    void shouldSearchUsersBy() throws Exception {
        SavorlyUser user1 = SavorlyUser.builder()
                .username("user1")
                .role(SavorlyRole.USER)
                .build();
        SavorlyUser user2 = SavorlyUser.builder()
                .username("user2")
                .role(SavorlyRole.ADMIN)
                .build();
        userRepository.saveAll(List.of(user1, user2));

        MvcResult actual = mockMvc.perform(get("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        List<UserResponse> response = objectMapper.readValue(actual.getResponse().getContentAsString(), new TypeReference<>() {
        });
        assertThat(response.size()).isEqualTo(2);
    }

    @Test
    @WithMockCustomUser(role = "admin")
    void shouldSearchUsersByFilter() throws Exception {
        SavorlyUser user1 = SavorlyUser.builder()
                .username("user1")
                .role(SavorlyRole.USER)
                .build();
        SavorlyUser user2 = SavorlyUser.builder()
                .username("user2")
                .role(SavorlyRole.ADMIN)
                .build();
        userRepository.saveAll(List.of(user1, user2));

        MvcResult actual = mockMvc.perform(get("/api/v1/users")
                        .queryParam("role", SavorlyRole.ADMIN.toString())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        List<UserResponse> response = objectMapper.readValue(actual.getResponse().getContentAsString(), new TypeReference<>() {
        });
        assertThat(response.size()).isEqualTo(1);
    }

    @Test
    @WithMockCustomUser()
    void shouldUpdateUser() throws Exception {
        SavorlyUser user = SavorlyUser.builder()
                .username("updateUser")
                .role(SavorlyRole.USER)
                .build();
        userRepository.save(user);

        UserDto updatedUser = UserDto.builder()
                .username("updateUser")
                .role(SavorlyRole.RESTAURANT_ADMIN)
                .build();

        mockMvc.perform(patch("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedUser)))
                .andExpect(status().isOk());

        SavorlyUser modifiedUser = userRepository.findByUsernameIgnoreCase("updateUser").orElseThrow();
        assertEquals(SavorlyRole.RESTAURANT_ADMIN, modifiedUser.getRole());
    }

    @Test
    @WithMockCustomUser
    void updateUser_isBadRequest() throws Exception {
        SavorlyUser user = SavorlyUser.builder()
                .username("updateUser")
                .role(SavorlyRole.USER)
                .build();
        userRepository.save(user);

        UserDto updatedUser = UserDto.builder()
                .username(null)
                .role(SavorlyRole.RESTAURANT_ADMIN)
                .build();

        mockMvc.perform(patch("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedUser)))
                .andExpect(status().isBadRequest());
    }
}