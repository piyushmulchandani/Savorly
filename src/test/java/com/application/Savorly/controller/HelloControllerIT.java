package com.application.Savorly.controller;

import com.application.Savorly.SavorlyApplication;
import com.application.Savorly.config.interfaces.WithMockCustomUser;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

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
}