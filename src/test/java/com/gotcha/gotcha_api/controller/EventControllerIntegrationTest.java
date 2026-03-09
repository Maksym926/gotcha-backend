package com.gotcha.gotcha_api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gotcha.gotcha_api.enums.Role;
import com.gotcha.gotcha_api.model.Event;
import com.gotcha.gotcha_api.model.User;
import com.gotcha.gotcha_api.model.dto.LoginRequest;
import com.gotcha.gotcha_api.repo.EventRepo;
import com.gotcha.gotcha_api.repo.UserRepo;
import com.gotcha.gotcha_api.service.S3Service;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.mock.web.MockPart;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDateTime;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Testcontainers
@Transactional
@TestPropertySource(locations = "classpath:application-test.properties")
public class EventControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private EventRepo eventRepo;

    @MockitoBean
    private S3Service s3Service;

    private String memberToken;
    private String adminToken;
    private Event testEvent;
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(12);

    @BeforeEach
    void setUp() throws Exception {
        when(s3Service.uploadFile(any(), anyString())).thenReturn("mocked-image-key.jpg");
        when(s3Service.generateSignedUrl(anyString())).thenReturn("https://mocked-url.com/image.jpg");

        // Create member user
        User member = new User();
        member.setUsername("eventmember");
        member.setEmail("eventmember@gotcha.com");
        member.setPassword(encoder.encode("password123"));
        member.setRole(Role.MEMBER);
        userRepo.save(member);

        // Create admin user
        User admin = new User();
        admin.setUsername("eventadmin");
        admin.setEmail("eventadmin@gotcha.com");
        admin.setPassword(encoder.encode("password123"));
        admin.setRole(Role.ADMIN);
        userRepo.save(admin);

        // Login member
        MvcResult memberLogin = mockMvc.perform(post("/api/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new LoginRequest("eventmember@gotcha.com", "password123"))))
                .andExpect(status().isOk())
                .andReturn();
        memberToken = memberLogin.getResponse().getContentAsString();

        // Login admin
        MvcResult adminLogin = mockMvc.perform(post("/api/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new LoginRequest("eventadmin@gotcha.com", "password123"))))
                .andExpect(status().isOk())
                .andReturn();
        adminToken = adminLogin.getResponse().getContentAsString();

        // Create test event
        testEvent = new Event();
        testEvent.setTitle("Coffee Tasting Night");
        testEvent.setDescription("Join us for an evening of premium coffee tasting");
        testEvent.setEventDate(LocalDateTime.now().plusDays(14));
        testEvent.setLocation("Gotcha Cafe, Paris");
        testEvent.setCreatedAt(LocalDateTime.now());
        testEvent.setImageKey("events-img/test-event.jpg");
        testEvent = eventRepo.save(testEvent);
    }

    // =========================================================
    // GET /api/member/event - Get all events
    // =========================================================

    @Test
    void testGetAllEvents_Success() throws Exception {
        mockMvc.perform(get("/api/member/event")
                        .header("Authorization", "Bearer " + memberToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].title", is("Coffee Tasting Night")));
    }

    @Test
    void testGetAllEvents_Empty() throws Exception {
        eventRepo.deleteAll();

        mockMvc.perform(get("/api/member/event")
                        .header("Authorization", "Bearer " + memberToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void testGetAllEvents_Unauthorized() throws Exception {
        mockMvc.perform(get("/api/member/event"))
                .andExpect(status().isUnauthorized());
    }

    // =========================================================
    // GET /api/member/event/{id} - Get event by ID
    // =========================================================

    @Test
    void testGetEventById_Success() throws Exception {
        mockMvc.perform(get("/api/member/event/" + testEvent.getEventId())
                        .header("Authorization", "Bearer " + memberToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title", is("Coffee Tasting Night")))
                .andExpect(jsonPath("$.description", is("Join us for an evening of premium coffee tasting")))
                .andExpect(jsonPath("$.location", is("Gotcha Cafe, Paris")));
    }

    @Test
    void testGetEventById_NotFound() throws Exception {
        mockMvc.perform(get("/api/member/event/9999")
                        .header("Authorization", "Bearer " + memberToken))
                .andExpect(status().isNotFound());
    }

    // =========================================================
    // POST /api/admin/event - Create event (admin only)
    // =========================================================

    @Test
    void testCreateEvent_AdminSuccess() throws Exception {
        Event newEvent = new Event();
        newEvent.setTitle("Latte Art Workshop");
        newEvent.setDescription("Learn to create beautiful latte art");
        newEvent.setEventDate(LocalDateTime.now().plusDays(30));
        newEvent.setLocation("Gotcha Cafe, Paris");
        newEvent.setCreatedAt(LocalDateTime.now());
        newEvent.setImageKey("placeholder");

        MockMultipartFile imageFile = new MockMultipartFile(
                "imageFile", "event.jpg", "image/jpeg", "fake-image".getBytes());

        MockPart eventPart = new MockPart("event", objectMapper.writeValueAsBytes(newEvent));
        eventPart.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        mockMvc.perform(multipart("/api/admin/event")
                        .file(imageFile)
                        .part(eventPart)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title", is("Latte Art Workshop")));
    }

    @Test
    void testCreateEvent_MemberForbidden() throws Exception {
        Event newEvent = new Event();
        newEvent.setTitle("Latte Art Workshop");
        newEvent.setDescription("Learn latte art");
        newEvent.setEventDate(LocalDateTime.now().plusDays(30));
        newEvent.setLocation("Gotcha Cafe");
        newEvent.setCreatedAt(LocalDateTime.now());
        newEvent.setImageKey("placeholder");

        MockMultipartFile imageFile = new MockMultipartFile(
                "imageFile", "event.jpg", "image/jpeg", "fake-image".getBytes());

        MockPart eventPart = new MockPart("event", objectMapper.writeValueAsBytes(newEvent));
        eventPart.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        mockMvc.perform(multipart("/api/admin/event")
                        .file(imageFile)
                        .part(eventPart)
                        .header("Authorization", "Bearer " + memberToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void testCreateEvent_Unauthorized() throws Exception {
        MockMultipartFile imageFile = new MockMultipartFile(
                "imageFile", "event.jpg", "image/jpeg", "fake-image".getBytes());

        mockMvc.perform(multipart("/api/admin/event")
                        .file(imageFile))
                .andExpect(status().isUnauthorized());
    }

    // =========================================================
    // PUT /api/admin/event/{id} - Update event (admin only)
    // =========================================================

    @Test
    void testUpdateEvent_AdminSuccess() throws Exception {
        Event updatedEvent = new Event();
        updatedEvent.setTitle("Updated Coffee Night");
        updatedEvent.setDescription("Updated description");
        updatedEvent.setEventDate(LocalDateTime.now().plusDays(21));
        updatedEvent.setLocation("New Location, Paris");
        updatedEvent.setCreatedAt(testEvent.getCreatedAt());
        updatedEvent.setImageKey("placeholder");

        MockMultipartFile imageFile = new MockMultipartFile(
                "imageFile", "updated.jpg", "image/jpeg", "fake-image".getBytes());

        MockPart eventPart = new MockPart("event", objectMapper.writeValueAsBytes(updatedEvent));
        eventPart.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        mockMvc.perform(multipart("/api/admin/event/" + testEvent.getEventId())
                        .file(imageFile)
                        .part(eventPart)
                        .header("Authorization", "Bearer " + adminToken)
                        .with(req -> { req.setMethod("PUT"); return req; }))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title", is("Updated Coffee Night")));
    }

    // =========================================================
    // DELETE /api/admin/event/{id} - Delete event (admin only)
    // =========================================================

    @Test
    void testDeleteEvent_AdminSuccess() throws Exception {
        mockMvc.perform(delete("/api/admin/event/" + testEvent.getEventId())
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(content().string("Deleted"));
    }

    @Test
    void testDeleteEvent_MemberForbidden() throws Exception {
        mockMvc.perform(delete("/api/admin/event/" + testEvent.getEventId())
                        .header("Authorization", "Bearer " + memberToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void testDeleteEvent_NotFound() throws Exception {
        mockMvc.perform(delete("/api/admin/event/9999")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNotFound());
    }
}
