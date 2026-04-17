package com.gotcha.gotcha_api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gotcha.gotcha_api.enums.AccountStatus;
import com.gotcha.gotcha_api.enums.Role;
import com.gotcha.gotcha_api.enums.SubscriptionStatus;
import com.gotcha.gotcha_api.exception.custom.RefundNotAllowedException;
import com.gotcha.gotcha_api.model.User;
import com.gotcha.gotcha_api.model.dto.LoginRequest;
import com.gotcha.gotcha_api.repo.UserRepo;
import com.gotcha.gotcha_api.service.EmailService;
import com.gotcha.gotcha_api.service.S3Service;
import com.gotcha.gotcha_api.service.SubscriptionService;
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

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Testcontainers
@Transactional
@TestPropertySource(locations = "classpath:application-test.properties")
public class SubscriptionControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepo userRepo;

    @MockitoBean
    private S3Service s3Service;

    @MockitoBean
    private SubscriptionService subscriptionService;

    @MockitoBean
    private EmailService emailService;

    private String memberToken;
    private User memberUser;
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(12);

    @BeforeEach
    void setUp() throws Exception {
        memberUser = new User();
        memberUser.setUsername("submember");
        memberUser.setEmail("submember@gotcha.com");
        memberUser.setPassword(encoder.encode("password123"));
        memberUser.setRole(Role.MEMBER);
        memberUser.setStatus(AccountStatus.ACTIVE);
        memberUser.setSubscriptionStatus(SubscriptionStatus.ACTIVE);
        memberUser.setStripeSubscriptionId("sub_test_123");
        memberUser = userRepo.save(memberUser);

        MvcResult memberLogin = mockMvc.perform(post("/api/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new LoginRequest("submember@gotcha.com", "password123"))))
                .andExpect(status().isOk())
                .andReturn();
        memberToken = memberLogin.getResponse().getContentAsString();
    }

    // =========================================================
    // POST /api/member/subscription - Subscribe
    // =========================================================

    @Test
    void testSubscribe_Success() throws Exception {
        when(subscriptionService.subscribe(any(User.class)))
                .thenReturn("pi_test_secret_123");

        mockMvc.perform(post("/api/member/subscription")
                        .header("Authorization", "Bearer " + memberToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.clientSecret", is("pi_test_secret_123")));
    }

    @Test
    void testSubscribe_Unauthorized() throws Exception {
        mockMvc.perform(post("/api/member/subscription"))
                .andExpect(status().isUnauthorized());
    }

    // =========================================================
    // DELETE /api/member/subscription - Cancel subscription
    // =========================================================

    @Test
    void testCancelSubscription_Success() throws Exception {
        doNothing().when(subscriptionService).cancelSubscription(any(User.class));

        mockMvc.perform(delete("/api/member/subscription")
                        .header("Authorization", "Bearer " + memberToken))
                .andExpect(status().isOk())
                .andExpect(content().string("Subscription will be cancelled at the end of the billing period"));
    }

    @Test
    void testCancelSubscription_Unauthorized() throws Exception {
        mockMvc.perform(delete("/api/member/subscription"))
                .andExpect(status().isUnauthorized());
    }

    // =========================================================
    // POST /api/member/subscription/refund - Refund subscription
    // =========================================================

    @Test
    void testRefundSubscription_Success() throws Exception {
        doNothing().when(subscriptionService).refundAsMember(any(User.class));

        mockMvc.perform(post("/api/member/subscription/refund")
                        .header("Authorization", "Bearer " + memberToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message", is("Subscription refunded successfully")));
    }

    @Test
    void testRefundSubscription_ExpiredWindow_Returns400() throws Exception {
        doThrow(new RefundNotAllowedException("Refund window has expired (7 days from last payment)"))
                .when(subscriptionService).refundAsMember(any(User.class));

        mockMvc.perform(post("/api/member/subscription/refund")
                        .header("Authorization", "Bearer " + memberToken))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.message", is("Refund window has expired (7 days from last payment)")));
    }

    @Test
    void testRefundSubscription_Unauthorized() throws Exception {
        mockMvc.perform(post("/api/member/subscription/refund"))
                .andExpect(status().isUnauthorized());
    }
}
