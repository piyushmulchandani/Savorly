package com.application.savorly.service;

import com.application.savorly.config.exceptions.NotFoundException;
import com.application.savorly.domain.entity.QSavorlyUser;
import com.application.savorly.domain.entity.SavorlyUser;
import com.application.savorly.dto.UserDto;
import com.application.savorly.dto.UserSearchParamsDto;
import com.application.savorly.repository.UserRepository;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Predicate;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final Keycloak keycloak;

    public UserService(UserRepository userRepository, Keycloak keycloak) {
        this.userRepository = userRepository;
        this.keycloak = keycloak;
    }

    public SavorlyUser findUserByUsername(String username) {
        return userRepository.findByUsernameIgnoreCase(username)
                .orElseThrow(() -> new NotFoundException("User not found with username " + username));
    }

    public void login(SavorlyUser user) {
        user.setLastLogonDate(new Date());
        userRepository.save(user);
    }

    public void createUser(UserDto userDto) {
        SavorlyUser savorlyUser = SavorlyUser.builder()
                .username(userDto.getUsername())
                .role(userDto.getRole())
                .lastLogonDate(new Date())
                .build();

        userRepository.save(savorlyUser);
    }

    public void deleteUser(SavorlyUser user) {
        String realm = "Savorly";
        UsersResource usersResource = keycloak.realm(realm).users();
        List<UserRepresentation> users = usersResource.search(user.getUsername());

        if (!users.isEmpty()) {
            String keycloakUserId = users.getFirst().getId();
            usersResource.get(keycloakUserId).remove();
        }

        userRepository.delete(user);
    }

    public List<SavorlyUser> searchUsersBy(UserSearchParamsDto userSearchParamsDto) {
        Predicate predicate = getWhere(userSearchParamsDto);
        return (List<SavorlyUser>) userRepository.findAll(predicate);
    }

    public void updateUser(SavorlyUser user, UserDto userDto) {
        user.setRole(userDto.getRole());

        userRepository.save(user);
    }

    private Predicate getWhere(UserSearchParamsDto userSearchParamsDto) {
        BooleanBuilder where = new BooleanBuilder();
        if (userSearchParamsDto.getUsername() != null) {
            where.and(QSavorlyUser.savorlyUser.username.eq(userSearchParamsDto.getUsername()));
        }
        if (userSearchParamsDto.getRole() != null) {
            where.and(QSavorlyUser.savorlyUser.role.eq(userSearchParamsDto.getRole()));
        }

        return where;
    }
}

