package com.application.Savorly.config.interfaces;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.test.context.support.WithSecurityContext;
import org.springframework.security.test.context.support.WithSecurityContextFactory;


import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Collections;

@Retention(RetentionPolicy.RUNTIME)
@WithSecurityContext(factory = WithMockCustomUser.WithMockCustomUserSecurityContextFactory.class)
public @interface WithMockCustomUser {
    String username() default "Username";
    String role() default "user";

    class WithMockCustomUserSecurityContextFactory implements WithSecurityContextFactory<WithMockCustomUser> {

        @Override
        public SecurityContext createSecurityContext(WithMockCustomUser annotation) {
            SecurityContext context = SecurityContextHolder.createEmptyContext();

            UserDetails userDetails = new User(
                    annotation.username(),
                    "password",
                    Collections.singletonList(new SimpleGrantedAuthority("ROLE_client_" + annotation.role()))
            );

            context.setAuthentication(new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                    userDetails, "password", userDetails.getAuthorities()));

            return context;
        }
    }
}
