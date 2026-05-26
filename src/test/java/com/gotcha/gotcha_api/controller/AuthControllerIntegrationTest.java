package com.gotcha.gotcha_api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gotcha.gotcha_api.enums.AccountStatus;
import com.gotcha.gotcha_api.enums.Role;
import com.gotcha.gotcha_api.enums.SubscriptionStatus;
import com.gotcha.gotcha_api.model.User;
import com.gotcha.gotcha_api.repo.UserRepo;
import com.gotcha.gotcha_api.service.EmailService;
import com.gotcha.gotcha_api.service.S3Service;
import com.gotcha.gotcha_api.service.SubscriptionService;
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
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Testcontainers
@Transactional
@TestPropertySource(locations = "classpath:application-test.properties")
public class AuthControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepo userRepo;

    @MockitoBean
    private S3Service s3Service;

    @MockitoBean
    private EmailService emailService;

    @MockitoBean
    private SubscriptionService subscriptionService;

    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(12);

    // =========================================================
    // POST /api/forgot-password
    // =========================================================

    @Test
    void testForgotPassword_PendingUser_DoesNotSendEmail() throws Exception {
        User pendingUser = new User();
        pendingUser.setUsername("pendinguser");
        pendingUser.setEmail("pending@gotcha.com");
        pendingUser.setPassword(encoder.encode("password123"));
        pendingUser.setRole(Role.MEMBER);
        pendingUser.setStatus(AccountStatus.PENDING);
        pendingUser.setSubscriptionStatus(SubscriptionStatus.INACTIVE);
        userRepo.save(pendingUser);

        mockMvc.perform(post("/api/forgot-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"pending@gotcha.com\"}"))
                .andExpect(status().isOk());

        verify(emailService, never()).sendPasswordResetEmail(anyString(), anyString());
    }

    // =========================================================
    // POST /api/resend-verification
    // =========================================================

    @Test
    void testResendVerification_PendingUser_SendsEmail() throws Exception {
        User pendingUser = new User();
        pendingUser.setUsername("pendinguser2");
        pendingUser.setEmail("pending2@gotcha.com");
        pendingUser.setPassword(encoder.encode("password123"));
        pendingUser.setRole(Role.MEMBER);
        pendingUser.setStatus(AccountStatus.PENDING);
        pendingUser.setSubscriptionStatus(SubscriptionStatus.INACTIVE);
        userRepo.save(pendingUser);

        mockMvc.perform(post("/api/resend-verification")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"pending2@gotcha.com\"}"))
                .andExpect(status().isOk());

        verify(emailService, times(1)).sendVerificationEmail(anyString(), anyString());
    }

    @Test
    void testResendVerification_ActiveUser_DoesNotSendEmail() throws Exception {
        User activeUser = new User();
        activeUser.setUsername("activeuser");
        activeUser.setEmail("active@gotcha.com");
        activeUser.setPassword(encoder.encode("password123"));
        activeUser.setRole(Role.MEMBER);
        activeUser.setStatus(AccountStatus.ACTIVE);
        activeUser.setSubscriptionStatus(SubscriptionStatus.INACTIVE);
        userRepo.save(activeUser);

        mockMvc.perform(post("/api/resend-verification")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"active@gotcha.com\"}"))
                .andExpect(status().isOk());

        verify(emailService, never()).sendVerificationEmail(anyString(), anyString());
    }

    @Test
    void testResendVerification_NonExistentEmail_Returns200() throws Exception {
        mockMvc.perform(post("/api/resend-verification")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"ghost@gotcha.com\"}"))
                .andExpect(status().isOk());

        verify(emailService, never()).sendVerificationEmail(anyString(), anyString());
    }
}
