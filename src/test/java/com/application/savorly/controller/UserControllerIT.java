package com.application.savorly.controller;

import com.application.savorly.SavorlyApplication;
import com.application.savorly.config.interfaces.WithMockCustomUser;
import com.application.savorly.domain.catalog.SavorlyRole;
import com.application.savorly.domain.entity.Restaurant;
import com.application.savorly.domain.entity.SavorlyUser;
import com.application.savorly.dto.response.UserResponseDto;
import com.application.savorly.dto.modify.UserModificationDto;
import com.application.savorly.repository.RestaurantRepository;
import com.application.savorly.repository.UserRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInstance;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.*;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
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

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
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

    @Autowired
    private RestaurantRepository restaurantRepository;

    @MockitoBean
    private Keycloak keycloak;

    @MockitoBean
    private RealmResource realmResource;

    @MockitoBean
    private RolesResource rolesResource;

    @MockitoBean
    private RoleResource roleResource;

    @MockitoBean
    private RoleRepresentation roleRepresentation;

    @MockitoBean
    private UsersResource usersResource;

    @MockitoBean
    private UserResource userResource;

    @MockitoBean
    private RoleMappingResource roleMappingResource;

    @MockitoBean
    private RoleScopeResource roleScopeResource;

    @BeforeEach
    public void setUp() {
        when(keycloak.realm(anyString())).thenReturn(realmResource);

        // Mock role retrieval
        when(realmResource.roles()).thenReturn(rolesResource);
        when(rolesResource.get(anyString())).thenReturn(roleResource);
        when(roleResource.toRepresentation()).thenReturn(roleRepresentation);

        // Mock user search
        when(realmResource.users()).thenReturn(usersResource);

        UserRepresentation mockUserRepresentation = new UserRepresentation();
        mockUserRepresentation.setId("user-id-123");

        when(usersResource.search(anyString()))
                .thenReturn(Collections.singletonList(mockUserRepresentation));

        // Mock getting a specific user
        when(usersResource.get(anyString())).thenReturn(userResource);

        // Mock user roles management
        when(userResource.roles()).thenReturn(roleMappingResource);
        when(roleMappingResource.realmLevel()).thenReturn(roleScopeResource);

        // Mock add and remove role methods
        doNothing().when(roleScopeResource).add(anyList());
        doNothing().when(roleScopeResource).remove(anyList());
    }

    @Test
    @WithMockCustomUser(username = "newUser")
    void shouldRegisterUser() throws Exception {
        String username = "newUser";

        mockMvc.perform(post("/api/v1/users/login/{username}", username))
                .andExpect(status().isOk());

        assertThat(userRepository.findByUsernameIgnoreCase(username)).isPresent();
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

        user = userRepository.findByUsernameIgnoreCase(user.getUsername()).orElseThrow();
        assertThat(user.getLastLogonDate()).isNotNull();
    }

    @Test
    @WithMockCustomUser(role = "restaurant_admin", username = "Admin")
    void shouldAddWorker() throws Exception {
        Restaurant restaurant = Restaurant.builder()
                .name("Restaurant")
                .build();
        restaurant = restaurantRepository.save(restaurant);

        SavorlyUser restaurantAdmin = SavorlyUser.builder()
                .username("Admin")
                .role(SavorlyRole.RESTAURANT_ADMIN)
                .build();
        restaurant.addWorker(restaurantAdmin);
        userRepository.save(restaurantAdmin);

        SavorlyUser newWorker = SavorlyUser.builder()
                .username("Worker")
                .role(SavorlyRole.USER)
                .build();
        userRepository.save(newWorker);

        mockMvc.perform(post("/api/v1/users/add-worker/{username}", newWorker.getUsername())
                        .queryParam("restaurantId", restaurant.getId().toString()))
                .andExpect(status().isOk());

        newWorker = userRepository.findByUsernameIgnoreCase(newWorker.getUsername()).orElseThrow();
        assertThat(newWorker.getRestaurant().getId()).isEqualTo(restaurant.getId());
        assertThat(newWorker.getRole()).isEqualTo(SavorlyRole.RESTAURANT_WORKER);
    }

    @Test
    @WithMockCustomUser(role = "restaurant_admin", username = "Admin")
    void addWorker_BadRequest() throws Exception {
        Restaurant restaurant = Restaurant.builder()
                .name("Restaurant")
                .build();
        restaurant = restaurantRepository.save(restaurant);

        SavorlyUser restaurantAdmin = SavorlyUser.builder()
                .username("Admin")
                .role(SavorlyRole.RESTAURANT_ADMIN)
                .build();
        restaurant.addWorker(restaurantAdmin);
        userRepository.save(restaurantAdmin);

        SavorlyUser newWorker = SavorlyUser.builder()
                .username("Worker")
                .role(SavorlyRole.USER)
                .build();
        restaurant.addWorker(newWorker);
        userRepository.save(newWorker);

        mockMvc.perform(post("/api/v1/users/add-worker/{username}", newWorker.getUsername())
                        .queryParam("restaurantId", restaurant.getId().toString()))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockCustomUser(role = "restaurant_admin", username = "Admin")
    void shouldRemoveWorker() throws Exception {
        Restaurant restaurant = Restaurant.builder()
                .name("Restaurant")
                .build();
        restaurant = restaurantRepository.save(restaurant);

        SavorlyUser restaurantAdmin = SavorlyUser.builder()
                .username("Admin")
                .role(SavorlyRole.RESTAURANT_ADMIN)
                .build();
        restaurant.addWorker(restaurantAdmin);
        userRepository.save(restaurantAdmin);

        SavorlyUser newWorker = SavorlyUser.builder()
                .username("Worker")
                .role(SavorlyRole.RESTAURANT_WORKER)
                .build();
        restaurant.addWorker(newWorker);
        userRepository.save(newWorker);

        mockMvc.perform(delete("/api/v1/users/remove-worker/{username}", newWorker.getUsername())
                        .queryParam("restaurantId", restaurant.getId().toString()))
                .andExpect(status().isOk());

        newWorker = userRepository.findByUsernameIgnoreCase(newWorker.getUsername()).orElseThrow();
        assertThat(newWorker.getRestaurant()).isNull();
        assertThat(newWorker.getRole()).isEqualTo(SavorlyRole.USER);
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

        assertThat(userRepository.findByUsernameIgnoreCase("deleteUser")).isEmpty();
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

        UserResponseDto response = objectMapper.readValue(actual.getResponse().getContentAsString(), UserResponseDto.class);
        assertThat(response.getUsername()).isEqualTo("testUser");
        assertThat(response.getRole()).isEqualTo(SavorlyRole.USER);
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

        List<UserResponseDto> response = objectMapper.readValue(actual.getResponse().getContentAsString(), new TypeReference<>() {
        });
        assertThat(response).hasSize(2);
    }

    @Test
    @WithMockCustomUser(role = "admin")
    void shouldSearchUsersByFilter() throws Exception {
        Restaurant restaurant = Restaurant.builder()
                .name("Restaurant")
                .build();
        restaurantRepository.save(restaurant);

        SavorlyUser user1 = SavorlyUser.builder()
                .username("user1")
                .role(SavorlyRole.USER)
                .build();
        SavorlyUser user2 = SavorlyUser.builder()
                .username("user2")
                .role(SavorlyRole.RESTAURANT_ADMIN)
                .restaurant(restaurant)
                .build();
        userRepository.saveAll(List.of(user1, user2));

        MvcResult actual = mockMvc.perform(get("/api/v1/users")
                        .queryParam("role", SavorlyRole.RESTAURANT_ADMIN.toString())
                        .queryParam("username", "user2")
                        .queryParam("restaurantName", "Restaurant")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        List<UserResponseDto> response = objectMapper.readValue(actual.getResponse().getContentAsString(), new TypeReference<>() {
        });
        assertThat(response).hasSize(1);
    }

    @Test
    @WithMockCustomUser(role = "admin")
    void shouldUpdateUser() throws Exception {
        SavorlyUser user = SavorlyUser.builder()
                .username("updateUser")
                .role(SavorlyRole.USER)
                .build();
        userRepository.save(user);

        Restaurant restaurant = Restaurant.builder()
                .name("Restaurant")
                .build();
        restaurantRepository.save(restaurant);

        UserModificationDto updatedUser = UserModificationDto.builder()
                .username("updateUser")
                .role(SavorlyRole.RESTAURANT_ADMIN)
                .restaurantName("Restaurant")
                .build();

        mockMvc.perform(patch("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedUser)))
                .andExpect(status().isOk());

        SavorlyUser modifiedUser = userRepository.findByUsernameIgnoreCase("updateUser").orElseThrow();
        assertThat(modifiedUser.getRole()).isEqualTo(SavorlyRole.RESTAURANT_ADMIN);
    }

    @Test
    @WithMockCustomUser(role = "admin")
    void updateUser_isBadRequest() throws Exception {
        SavorlyUser user = SavorlyUser.builder()
                .username("updateUser")
                .role(SavorlyRole.USER)
                .build();
        userRepository.save(user);

        UserModificationDto updatedUser = UserModificationDto.builder()
                .username(null)
                .role(SavorlyRole.RESTAURANT_ADMIN)
                .build();

        mockMvc.perform(patch("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedUser)))
                .andExpect(status().isBadRequest());
    }
}