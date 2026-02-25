package com.gotcha.gotcha_api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gotcha.gotcha_api.enums.Role;
import com.gotcha.gotcha_api.model.dto.LoginRequest;
import com.gotcha.gotcha_api.model.dto.RegisterRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Testcontainers
@Transactional
@TestPropertySource(locations = "classpath:application-test.properties")
class AuthControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void testRegisterUser_Success() throws Exception {
        RegisterRequest registerRequest = new RegisterRequest(
                "testuser",
                "testuser@example.com",
                "password123"
        );

        mockMvc.perform(post("/api/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userName", is("testuser")))
                .andExpect(jsonPath("$.email", is("testuser@example.com")))
                .andExpect(jsonPath("$.id", notNullValue()));
    }

    @Test
    void testRegisterUser_DuplicateEmail() throws Exception {
        RegisterRequest firstUser = new RegisterRequest(
                "user1",
                "duplicate@example.com",
                "password123"
        );

        // Register first user
        mockMvc.perform(post("/api/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(firstUser)))
                .andExpect(status().isOk());

        RegisterRequest duplicateUser = new RegisterRequest(
                "user2",
                "duplicate@example.com",
                "password456"
        );

        // Attempt to register with duplicate email
        mockMvc.perform(post("/api/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(duplicateUser)))
                .andExpect(status().is4xxClientError());
    }

    @Test
    void testLoginUser_Success() throws Exception {
        // First register a user
        RegisterRequest registerRequest = new RegisterRequest(
                "loginuser",
                "loginuser@example.com",
                "password123"
        );

        mockMvc.perform(post("/api/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isOk());

        // Then login with the registered user
        LoginRequest loginRequest = new LoginRequest(
                "loginuser@example.com",
                "password123",
                Role.MEMBER
        );

        mockMvc.perform(post("/api/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(content().string(not(containsString("Failed to login"))))
                .andExpect(content().string(notNullValue()));
    }

    @Test
    void testLoginUser_InvalidCredentials() throws Exception {
        LoginRequest loginRequest = new LoginRequest(
                "nonexistent@example.com",
                "wrongpassword",
                Role.MEMBER
        );

        mockMvc.perform(post("/api/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().is4xxClientError());
    }

    @Test
    void testLoginUser_WrongPassword() throws Exception {
        // First register a user
        RegisterRequest registerRequest = new RegisterRequest(
                "testpassword",
                "testpassword@example.com",
                "correctpassword"
        );

        mockMvc.perform(post("/api/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isOk());

        // Attempt login with wrong password
        LoginRequest loginRequest = new LoginRequest(
                "testpassword@example.com",
                "wrongpassword",
                Role.MEMBER
        );

        mockMvc.perform(post("/api/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().is4xxClientError());
    }
}
