package com.gotcha.gotcha_api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gotcha.gotcha_api.enums.Role;
import com.gotcha.gotcha_api.enums.SubscriptionStatus;
import com.gotcha.gotcha_api.model.User;
import com.gotcha.gotcha_api.model.dto.LoginRequest;
import com.gotcha.gotcha_api.model.dto.ProfileRequest;
import com.gotcha.gotcha_api.repo.UserRepo;
import com.gotcha.gotcha_api.service.S3Service;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.mock.web.MockPart;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Testcontainers
@Transactional
@TestPropertySource(locations = "classpath:application-test.properties")
public class ProfileIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepo userRepository;

    @MockitoBean
    private S3Service s3Service;

    private User testUser;
    private String jwtToken;
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(12);

    @BeforeEach
    void setUp() throws Exception {
        testUser = new User();
        testUser.setUsername("profileuser");
        testUser.setEmail("profileuser@gotcha.com");
        testUser.setPassword(encoder.encode("password123"));
        testUser.setRole(Role.MEMBER);
        testUser.setSubscriptionStatus(SubscriptionStatus.ACTIVE);
        userRepository.save(testUser);

        when(s3Service.uploadFile(any(), anyString())).thenReturn("mocked-image-key");
        when(s3Service.generateSignedUrl(anyString())).thenReturn("https://mocked-url.com/image.jpg");

        LoginRequest loginRequest = new LoginRequest(
                "profileuser@gotcha.com",
                "password123"
        );

        MvcResult loginResult = mockMvc.perform(post("/api/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn();

        jwtToken = loginResult.getResponse().getContentAsString();
    }

    @Test
    void testUpdateProfile_Success() throws Exception {
        ProfileRequest profileRequest = new ProfileRequest(
                "updateduser",
                "happy",
                "Matcha Latte"
        );

        MockMultipartFile profileImage = new MockMultipartFile(
                "profileImage",
                "photo.jpg",
                "image/jpeg",
                "fake-image-bytes".getBytes()
        );

        MockPart profilePart = new MockPart(
                "profile",
                objectMapper.writeValueAsBytes(profileRequest)
        );
        profilePart.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        mockMvc.perform(multipart("/api/member/profile")
                        .file(profileImage)
                        .part(profilePart)
                        .header("Authorization", "Bearer " + jwtToken)
                        .with(req -> { req.setMethod("PUT"); return req; }))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username", is("updateduser")))
                .andExpect(jsonPath("$.mood", is("happy")))
                .andExpect(jsonPath("$.gotchaFavDrink", is("Matcha Latte")));

        verify(s3Service, times(1)).uploadFile(any(), anyString());
    }

    @Test
    void testUpdateProfile_NoImage() throws Exception {
        ProfileRequest profileRequest = new ProfileRequest(
                "nouserimage",
                "calm",
                "Taro"
        );

        MockMultipartFile emptyImage = new MockMultipartFile(
                "profileImage",
                "",
                "image/jpeg",
                new byte[0]
        );

        MockPart profilePart = new MockPart(
                "profile",
                objectMapper.writeValueAsBytes(profileRequest)
        );
        profilePart.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        mockMvc.perform(multipart("/api/member/profile")
                        .file(emptyImage)
                        .part(profilePart)
                        .header("Authorization", "Bearer " + jwtToken)
                        .with(req -> { req.setMethod("PUT"); return req; }))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username", is("nouserimage")))
                .andExpect(jsonPath("$.mood", is("calm")))
                .andExpect(jsonPath("$.gotchaFavDrink", is("Taro")));

        verify(s3Service, never()).uploadFile(any(), anyString());
    }

    @Test
    void testUpdateProfile_Unauthorized() throws Exception {
        ProfileRequest profileRequest = new ProfileRequest(
                "hacker",
                "sneaky",
                "Nothing"
        );

        MockMultipartFile profileImage = new MockMultipartFile(
                "profileImage",
                "photo.jpg",
                "image/jpeg",
                "fake-image-bytes".getBytes()
        );

        MockPart profilePart = new MockPart(
                "profile",
                objectMapper.writeValueAsBytes(profileRequest)
        );
        profilePart.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        mockMvc.perform(multipart("/api/member/profile")
                        .file(profileImage)
                        .part(profilePart)
                        .with(req -> { req.setMethod("PUT"); return req; }))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testUpdateProfile_ValidationFails() throws Exception {
        ProfileRequest profileRequest = new ProfileRequest(
                "a",    // too short — @Size(min = 2) violation
                "ok",
                "Tea"
        );

        MockMultipartFile profileImage = new MockMultipartFile(
                "profileImage",
                "photo.jpg",
                "image/jpeg",
                "fake-image-bytes".getBytes()
        );

        MockPart profilePart = new MockPart(
                "profile",
                objectMapper.writeValueAsBytes(profileRequest)
        );
        profilePart.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        mockMvc.perform(multipart("/api/member/profile")
                        .file(profileImage)
                        .part(profilePart)
                        .header("Authorization", "Bearer " + jwtToken)
                        .with(req -> { req.setMethod("PUT"); return req; }))
                .andExpect(status().isBadRequest());
    }
}
