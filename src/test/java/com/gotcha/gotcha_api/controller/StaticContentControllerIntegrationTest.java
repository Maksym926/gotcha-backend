package com.gotcha.gotcha_api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gotcha.gotcha_api.enums.Role;
import com.gotcha.gotcha_api.model.StaticContent;
import com.gotcha.gotcha_api.model.User;
import com.gotcha.gotcha_api.model.dto.LoginRequest;
import com.gotcha.gotcha_api.repo.StaticContentRepo;
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
public class StaticContentControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private StaticContentRepo staticContentRepo;

    @MockitoBean
    private S3Service s3Service;

    private String memberToken;
    private String adminToken;
    private StaticContent testContent;
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(12);

    @BeforeEach
    void setUp() throws Exception {
        when(s3Service.uploadFile(any(), anyString())).thenReturn("mocked-image-key.jpg");
        when(s3Service.generateSignedUrl(anyString())).thenReturn("https://mocked-url.com/image.jpg");

        // Create member user
        User member = new User();
        member.setUsername("contentmember");
        member.setEmail("contentmember@gotcha.com");
        member.setPassword(encoder.encode("password123"));
        member.setRole(Role.MEMBER);
        userRepo.save(member);

        // Create admin user
        User admin = new User();
        admin.setUsername("contentadmin");
        admin.setEmail("contentadmin@gotcha.com");
        admin.setPassword(encoder.encode("password123"));
        admin.setRole(Role.ADMIN);
        userRepo.save(admin);

        // Login member
        MvcResult memberLogin = mockMvc.perform(post("/api/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new LoginRequest("contentmember@gotcha.com", "password123"))))
                .andExpect(status().isOk())
                .andReturn();
        memberToken = memberLogin.getResponse().getContentAsString();

        // Login admin
        MvcResult adminLogin = mockMvc.perform(post("/api/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new LoginRequest("contentadmin@gotcha.com", "password123"))))
                .andExpect(status().isOk())
                .andReturn();
        adminToken = adminLogin.getResponse().getContentAsString();

        // Create test static content
        testContent = new StaticContent();
        testContent.setSectionKey("hero-banner");
        testContent.setTitle("Welcome to Gotcha Cafe");
        testContent.setDescription("The best cafe in Paris");
        testContent.setImageKey("static-content-img/hero.jpg");
        testContent = staticContentRepo.save(testContent);
    }

    // =========================================================
    // GET /api/member/static-content - Get all sections
    // =========================================================

    @Test
    void testGetAllSections_Success() throws Exception {
        mockMvc.perform(get("/api/member/static-content")
                        .header("Authorization", "Bearer " + memberToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].sectionKey", is("hero-banner")))
                .andExpect(jsonPath("$[0].title", is("Welcome to Gotcha Cafe")));
    }

    @Test
    void testGetAllSections_Empty() throws Exception {
        staticContentRepo.deleteAll();

        mockMvc.perform(get("/api/member/static-content")
                        .header("Authorization", "Bearer " + memberToken))
                .andExpect(status().isNotFound());
    }

    @Test
    void testGetAllSections_Unauthorized() throws Exception {
        mockMvc.perform(get("/api/member/static-content"))
                .andExpect(status().isUnauthorized());
    }

    // =========================================================
    // GET /api/member/static-content/{sectionKey} - Get by key
    // =========================================================

    @Test
    void testGetSectionByKey_Success() throws Exception {
        mockMvc.perform(get("/api/member/static-content/hero-banner")
                        .header("Authorization", "Bearer " + memberToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sectionKey", is("hero-banner")))
                .andExpect(jsonPath("$.title", is("Welcome to Gotcha Cafe")))
                .andExpect(jsonPath("$.description", is("The best cafe in Paris")));
    }

    @Test
    void testGetSectionByKey_NotFound() throws Exception {
        mockMvc.perform(get("/api/member/static-content/nonexistent-key")
                        .header("Authorization", "Bearer " + memberToken))
                .andExpect(status().isNotFound());
    }

    // =========================================================
    // PUT /api/admin/static-content/{sectionKey} - Update (admin)
    // =========================================================

    @Test
    void testUpdateSection_AdminSuccess() throws Exception {
        StaticContent updatedContent = new StaticContent();
        updatedContent.setSectionKey("hero-banner");
        updatedContent.setTitle("Updated Title");
        updatedContent.setDescription("Updated description for Gotcha Cafe");
        updatedContent.setImageKey("placeholder");

        MockMultipartFile imageFile = new MockMultipartFile(
                "imageFile", "hero.jpg", "image/jpeg", "fake-image".getBytes());

        MockPart contentPart = new MockPart("content", objectMapper.writeValueAsBytes(updatedContent));
        contentPart.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        mockMvc.perform(multipart("/api/admin/static-content/hero-banner")
                        .file(imageFile)
                        .part(contentPart)
                        .header("Authorization", "Bearer " + adminToken)
                        .with(req -> { req.setMethod("PUT"); return req; }))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title", is("Updated Title")));
    }

    @Test
    void testUpdateSection_MemberForbidden() throws Exception {
        StaticContent updatedContent = new StaticContent();
        updatedContent.setSectionKey("hero-banner");
        updatedContent.setTitle("Hacked Title");
        updatedContent.setDescription("Hacked");
        updatedContent.setImageKey("placeholder");

        MockMultipartFile imageFile = new MockMultipartFile(
                "imageFile", "hero.jpg", "image/jpeg", "fake-image".getBytes());

        MockPart contentPart = new MockPart("content", objectMapper.writeValueAsBytes(updatedContent));
        contentPart.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        mockMvc.perform(multipart("/api/admin/static-content/hero-banner")
                        .file(imageFile)
                        .part(contentPart)
                        .header("Authorization", "Bearer " + memberToken)
                        .with(req -> { req.setMethod("PUT"); return req; }))
                .andExpect(status().isForbidden());
    }

    @Test
    void testUpdateSection_Unauthorized() throws Exception {
        MockMultipartFile imageFile = new MockMultipartFile(
                "imageFile", "hero.jpg", "image/jpeg", "fake-image".getBytes());

        mockMvc.perform(multipart("/api/admin/static-content/hero-banner")
                        .file(imageFile)
                        .with(req -> { req.setMethod("PUT"); return req; }))
                .andExpect(status().isUnauthorized());
    }
}
