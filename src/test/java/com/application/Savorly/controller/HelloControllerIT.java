package com.application.Savorly.controller;

import com.application.Savorly.SavorlyApplication;
import com.application.Savorly.config.interfaces.WithMockCustomUser;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
@AutoConfigureMockMvc
@SpringBootTest(classes = SavorlyApplication.class)
class HelloControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @WithMockCustomUser()
    void hello() throws Exception {
        MvcResult actual = mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/hello")
                .queryParam("name", "Name"))
                .andExpect(status().isOk()).andReturn();

        String actualString = actual.getResponse().getContentAsString();
        assertEquals("Hello, Name!", actualString);
    }

    @Test
    void shouldReturnUnauthorizedForUnauthenticatedUser() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/hello")
                        .queryParam("name", "Name"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockCustomUser(username = "adminUser", role = "admin")
    void shouldReturnAuthenticatedUser() throws Exception {
        UserDetails userDetails = new User("adminUser", "password",
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_client_admin")));
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                userDetails, "password", userDetails.getAuthorities()));
        SecurityContextHolder.setContext(context);

        MvcResult actual = mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/users/me"))
                .andExpect(status().isOk())
                .andReturn();
    }
}