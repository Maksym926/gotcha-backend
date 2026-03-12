package com.gotcha.gotcha_api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gotcha.gotcha_api.enums.Role;
import com.gotcha.gotcha_api.enums.SubscriptionStatus;
import com.gotcha.gotcha_api.model.User;
import com.gotcha.gotcha_api.model.dto.LoginRequest;
import com.gotcha.gotcha_api.repo.UserRepo;
import com.gotcha.gotcha_api.service.S3Service;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.hamcrest.Matchers.*;
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
public class AdminControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepo userRepo;

    @MockitoBean
    private S3Service s3Service;

    private String adminToken;
    private String memberToken;
    private User adminUser;
    private User memberUser;
    private User targetUser;
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(12);

    @BeforeEach
    void setUp() throws Exception {
        when(s3Service.generateSignedUrl(anyString())).thenReturn("https://mocked-url.com/image.jpg");

        // Create admin user
        adminUser = new User();
        adminUser.setUsername("superadmin");
        adminUser.setEmail("superadmin@gotcha.com");
        adminUser.setPassword(encoder.encode("password123"));
        adminUser.setRole(Role.ADMIN);
        adminUser = userRepo.save(adminUser);

        // Create member user
        memberUser = new User();
        memberUser.setUsername("regularmember");
        memberUser.setEmail("regularmember@gotcha.com");
        memberUser.setPassword(encoder.encode("password123"));
        memberUser.setRole(Role.MEMBER);
        memberUser.setSubscriptionStatus(SubscriptionStatus.ACTIVE);
        memberUser = userRepo.save(memberUser);

        // Create target user (for get/delete tests)
        targetUser = new User();
        targetUser.setUsername("targetuser");
        targetUser.setEmail("targetuser@gotcha.com");
        targetUser.setPassword(encoder.encode("password123"));
        targetUser.setRole(Role.MEMBER);
        targetUser.setSubscriptionStatus(SubscriptionStatus.ACTIVE);
        targetUser = userRepo.save(targetUser);

        // Login admin
        MvcResult adminLogin = mockMvc.perform(post("/api/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new LoginRequest("superadmin@gotcha.com", "password123"))))
                .andExpect(status().isOk())
                .andReturn();
        adminToken = adminLogin.getResponse().getContentAsString();

        // Login member
        MvcResult memberLogin = mockMvc.perform(post("/api/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new LoginRequest("regularmember@gotcha.com", "password123"))))
                .andExpect(status().isOk())
                .andReturn();
        memberToken = memberLogin.getResponse().getContentAsString();
    }

    // =========================================================
    // GET /api/admin/users - Get all users
    // =========================================================

    @Test
    void testGetAllUsers_AdminSuccess() throws Exception {
        mockMvc.perform(get("/api/admin/users")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[*].username", hasItems("superadmin", "regularmember", "targetuser")));
    }

    @Test
    void testGetAllUsers_MemberForbidden() throws Exception {
        mockMvc.perform(get("/api/admin/users")
                        .header("Authorization", "Bearer " + memberToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void testGetAllUsers_Unauthorized() throws Exception {
        mockMvc.perform(get("/api/admin/users"))
                .andExpect(status().isUnauthorized());
    }

    // =========================================================
    // GET /api/admin/users/{id} - Get user by ID
    // =========================================================

    @Test
    void testGetUserById_AdminSuccess() throws Exception {
        mockMvc.perform(get("/api/admin/users/" + targetUser.getUserId())
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username", is("targetuser")))
                .andExpect(jsonPath("$.email", is("targetuser@gotcha.com")));
    }

    @Test
    void testGetUserById_NotFound() throws Exception {
        mockMvc.perform(get("/api/admin/users/9999")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNotFound());
    }

    @Test
    void testGetUserById_MemberForbidden() throws Exception {
        mockMvc.perform(get("/api/admin/users/" + targetUser.getUserId())
                        .header("Authorization", "Bearer " + memberToken))
                .andExpect(status().isForbidden());
    }

    // =========================================================
    // DELETE /api/admin/users/{id} - Delete user
    // =========================================================

    @Test
    void testDeleteUser_AdminSuccess() throws Exception {
        mockMvc.perform(delete("/api/admin/users/" + targetUser.getUserId())
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(content().string("User deleted successfully"));
    }

    @Test
    void testDeleteUser_MemberForbidden() throws Exception {
        mockMvc.perform(delete("/api/admin/users/" + targetUser.getUserId())
                        .header("Authorization", "Bearer " + memberToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void testDeleteUser_Unauthorized() throws Exception {
        mockMvc.perform(delete("/api/admin/users/" + targetUser.getUserId()))
                .andExpect(status().isUnauthorized());
    }
}
