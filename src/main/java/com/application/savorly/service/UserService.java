package com.application.savorly.service;

import com.application.savorly.config.exceptions.NotFoundException;
import com.application.savorly.domain.catalog.SavorlyRole;
import com.application.savorly.domain.entity.QSavorlyUser;
import com.application.savorly.domain.entity.Restaurant;
import com.application.savorly.domain.entity.SavorlyUser;
import com.application.savorly.dto.modify.UserModificationDto;
import com.application.savorly.dto.search.UserSearchDto;
import com.application.savorly.repository.UserRepository;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Predicate;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Date;
import java.util.List;

@Slf4j
@Service
public class UserService {

    private final UserRepository userRepository;
    private final Keycloak keycloak;
    private final RestaurantService restaurantService;

    private static final String REALM = "Savorly";

    public UserService(UserRepository userRepository, Keycloak keycloak, RestaurantService restaurantService) {
        this.userRepository = userRepository;
        this.keycloak = keycloak;
        this.restaurantService = restaurantService;
    }

    public SavorlyUser findUserByUsername(String username) {
        return userRepository.findByUsernameIgnoreCase(username)
                .orElseThrow(() -> new NotFoundException("User not found with username " + username));
    }

    public void login(SavorlyUser user) {
        user.setLastLogonDate(new Date());
        userRepository.save(user);
    }

    public void createUser(UserModificationDto userModificationDto) {
        SavorlyUser savorlyUser = SavorlyUser.builder()
                .username(userModificationDto.getUsername())
                .role(userModificationDto.getRole())
                .lastLogonDate(new Date())
                .build();

        userRepository.save(savorlyUser);
    }

    public void deleteUser(SavorlyUser user) {
        UsersResource usersResource = keycloak.realm(REALM).users();
        List<UserRepresentation> users = usersResource.search(user.getUsername());

        if (!users.isEmpty()) {
            String keycloakUserId = users.getFirst().getId();
            usersResource.get(keycloakUserId).remove();
        }

        userRepository.delete(user);
    }

    public List<SavorlyUser> searchUsersBy(UserSearchDto userSearchDto) {
        Predicate predicate = getWhere(userSearchDto);
        return (List<SavorlyUser>) userRepository.findAll(predicate);
    }

    public void updateUser(SavorlyUser user, UserModificationDto userModificationDto) {
        if (userModificationDto.getRestaurantName() != null) {
            Restaurant restaurant = restaurantService.findByName(userModificationDto.getRestaurantName());
            restaurant.addWorker(user);
        }
        if (userModificationDto.getRole() != null) {
            removeRoleFromUser(user.getUsername(), user.getRole().toString());
            user.setRole(userModificationDto.getRole());
            assignRoleToUser(user.getUsername(), userModificationDto.getRole().toString());
        }
        userRepository.save(user);
    }

    public void addRestaurantAdmin(SavorlyUser user, Restaurant restaurant) {
        user.setRole(SavorlyRole.RESTAURANT_ADMIN);
        assignRoleToUser(user.getUsername(), SavorlyRole.RESTAURANT_ADMIN.toString());
        restaurant.addWorker(user);

        userRepository.save(user);
    }

    public void addRestaurantWorker(SavorlyUser user, Restaurant restaurant) {
        user.setRole(SavorlyRole.RESTAURANT_WORKER);
        assignRoleToUser(user.getUsername(), SavorlyRole.RESTAURANT_WORKER.toString());
        restaurant.addWorker(user);

        userRepository.save(user);
    }

    public void removeFromRestaurant(SavorlyUser user) {
        removeRoleFromUser(user.getUsername(), user.getRole().toString());
        user.setRole(SavorlyRole.USER);
        user.setRestaurant(null);

        userRepository.save(user);
    }

    private Predicate getWhere(UserSearchDto userSearchDto) {
        BooleanBuilder where = new BooleanBuilder();
        if (userSearchDto.getUsername() != null) {
            where.and(QSavorlyUser.savorlyUser.username.contains(userSearchDto.getUsername()));
        }
        if (userSearchDto.getRole() != null) {
            where.and(QSavorlyUser.savorlyUser.role.eq(userSearchDto.getRole()));
        }
        if (userSearchDto.getRestaurantName() != null) {
            where.and((QSavorlyUser.savorlyUser.restaurant.name.contains(userSearchDto.getRestaurantName())));
        }

        return where;
    }

    private void assignRoleToUser(String username, String roleName) {
        RoleRepresentation role = keycloak.realm(REALM)
                .roles()
                .get(roleName.toLowerCase())
                .toRepresentation();

        UserRepresentation user = keycloak.realm(REALM)
                .users()
                .search(username)
                .stream()
                .findFirst()
                .orElseThrow(() -> new NotFoundException("User not found"));

        keycloak.realm(REALM)
                .users()
                .get(user.getId())
                .roles()
                .realmLevel()
                .add(Collections.singletonList(role));
    }

    private void removeRoleFromUser(String username, String roleName) {
        RoleRepresentation role = keycloak.realm(REALM)
                .roles()
                .get(roleName.toLowerCase())
                .toRepresentation();

        UserRepresentation user = keycloak.realm(REALM)
                .users()
                .search(username)
                .stream()
                .findFirst()
                .orElseThrow(() -> new NotFoundException("User not found"));

        keycloak.realm(REALM)
                .users()
                .get(user.getId())
                .roles()
                .realmLevel()
                .remove(Collections.singletonList(role));
    }
}

