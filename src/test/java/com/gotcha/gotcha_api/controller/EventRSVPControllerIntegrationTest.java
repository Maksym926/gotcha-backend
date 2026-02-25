package com.gotcha.gotcha_api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gotcha.gotcha_api.enums.Role;
import com.gotcha.gotcha_api.model.Event;
import com.gotcha.gotcha_api.model.EventRSVP;
import com.gotcha.gotcha_api.model.User;
import com.gotcha.gotcha_api.model.dto.LoginRequest;
import com.gotcha.gotcha_api.model.dto.RSVPRequest;
import com.gotcha.gotcha_api.model.dto.RegisterRequest;
import com.gotcha.gotcha_api.model.dto.UpdateRSVPRequest;
import com.gotcha.gotcha_api.repo.EventRSVPRepo;
import com.gotcha.gotcha_api.repo.EventRepo;

import com.gotcha.gotcha_api.repo.UserRepo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDateTime;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Testcontainers
@Transactional
@TestPropertySource(locations = "classpath:application-test.properties")
class EventRSVPControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepo userRepository;

    @Autowired
    private EventRepo eventRepository;

    @Autowired
    private EventRSVPRepo eventRSVPRepository;

    private User testUser;
    private Event testEvent;
    private String jwtToken;

    @BeforeEach
    void setUp() throws Exception {
        // Register and login a test user to get JWT token
        RegisterRequest registerRequest = new RegisterRequest(
                "rsvpuser",
                "rsvpuser@example.com",
                "password123"
        );

        mockMvc.perform(post("/api/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isOk());

        LoginRequest loginRequest = new LoginRequest(
                "rsvpuser@example.com",
                "password123",
                Role.MEMBER
        );

        MvcResult loginResult = mockMvc.perform(post("/api/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn();

        jwtToken = loginResult.getResponse().getContentAsString();
    }

    @Test
    @WithMockUser(username = "rsvpuser@example.com", roles = {"MEMBER"})
    void testSubmitRSVP_Success() throws Exception {
        RSVPRequest rsvpRequest = new RSVPRequest(
                1L,
                "John Doe",
                "john.doe@example.com",
                2L
        );

        mockMvc.perform(post("/api/member/event/rsvp")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(rsvpRequest)))
                .andExpect(status().isOk())
                .andExpect(content().string("RSVP submitted successfully"));
    }

    @Test
    @WithMockUser(username = "rsvpuser@example.com", roles = {"MEMBER"})
    void testUpdateRSVP_Success() throws Exception {
        // First submit an RSVP
        RSVPRequest rsvpRequest = new RSVPRequest(
                1L,
                "Jane Doe",
                "jane.doe@example.com",
                1L
        );

        MvcResult submitResult = mockMvc.perform(post("/api/member/event/rsvp")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(rsvpRequest)))
                .andExpect(status().isOk())
                .andReturn();

        // Assume RSVP ID is 1 (or retrieve it from database)
        Long rsvpId = 1L;

        UpdateRSVPRequest updateRequest = new UpdateRSVPRequest(
                "Jane Smith",
                "jane.smith@example.com",
                3L
        );

        mockMvc.perform(put("/api/member/event/rsvp/" + rsvpId)
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(content().string("RSVP updated successfully"));
    }

    @Test
    @WithMockUser(username = "rsvpuser@example.com", roles = {"MEMBER"})
    void testGetRSVPById_Success() throws Exception {
        // First submit an RSVP
        RSVPRequest rsvpRequest = new RSVPRequest(
                1L,
                "Test User",
                "test@example.com",
                2L
        );

        mockMvc.perform(post("/api/member/event/rsvp")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(rsvpRequest)))
                .andExpect(status().isOk());

        Long rsvpId = 1L;

        mockMvc.perform(get("/api/member/event/rsvp/" + rsvpId)
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rsvpId", notNullValue()))
                .andExpect(jsonPath("$.rsvpName", is("Test User")))
                .andExpect(jsonPath("$.rsvpEmail", is("test@example.com")));
    }

    @Test
    @WithMockUser(username = "rsvpuser@example.com", roles = {"MEMBER"})
    void testDeleteRSVP_Success() throws Exception {
        // First submit an RSVP
        RSVPRequest rsvpRequest = new RSVPRequest(
                1L,
                "Delete User",
                "delete@example.com",
                1L
        );

        mockMvc.perform(post("/api/member/event/rsvp")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(rsvpRequest)))
                .andExpect(status().isOk());

        Long rsvpId = 1L;

        mockMvc.perform(delete("/api/member/event/rsvp/" + rsvpId)
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(content().string("RSVP deleted successfully"));

        // Verify deletion
        mockMvc.perform(get("/api/member/event/rsvp/" + rsvpId)
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().is4xxClientError());
    }

    @Test
    @WithMockUser(username = "admin@example.com", roles = {"ADMIN"})
    void testGetRSVPByUserId_Admin() throws Exception {
        Long userId = 1L;

        mockMvc.perform(get("/api/admin/user/" + userId + "/rsvp")
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", isA(java.util.List.class)));
    }

    @Test
    @WithMockUser(username = "admin@example.com", roles = {"ADMIN"})
    void testGetRSVPByEventId_Admin() throws Exception {
        Long eventId = 1L;

        mockMvc.perform(get("/api/admin/event/" + eventId + "/rsvp")
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", isA(java.util.List.class)));
    }

    @Test
    @WithMockUser(username = "admin@example.com", roles = {"ADMIN"})
    void testGetAllRSVP_Admin() throws Exception {
        mockMvc.perform(get("/api/admin/event/rsvp")
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", isA(java.util.List.class)));
    }

    @Test
    void testSubmitRSVP_Unauthorized() throws Exception {
        RSVPRequest rsvpRequest = new RSVPRequest(
                1L,
                "Unauthorized User",
                "unauthorized@example.com",
                1L
        );

        mockMvc.perform(post("/api/member/event/rsvp")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(rsvpRequest)))
                .andExpect(status().is4xxClientError());
    }
}
