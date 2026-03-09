package com.gotcha.gotcha_api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gotcha.gotcha_api.enums.Role;
import com.gotcha.gotcha_api.enums.SubscriptionStatus;
import com.gotcha.gotcha_api.model.User;
import com.gotcha.gotcha_api.repo.UserRepo;
import com.gotcha.gotcha_api.service.S3Service;
import com.stripe.model.Event;
import com.stripe.model.StripeObject;
import com.stripe.net.Webhook;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Testcontainers
@Transactional
@TestPropertySource(locations = "classpath:application-test.properties")
public class WebhookControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepo userRepo;

    @MockitoBean
    private S3Service s3Service;

    @Value("${stripe.webhook.secret}")
    private String webhookSecret;

    private User testUser;
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(12);

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setUsername("webhookuser");
        testUser.setEmail("webhookuser@gotcha.com");
        testUser.setPassword(encoder.encode("password123"));
        testUser.setRole(Role.MEMBER);
        testUser.setStripeCustomerId("cus_test_123");
        testUser.setSubscriptionStatus(SubscriptionStatus.INACTIVE);
        testUser = userRepo.save(testUser);
    }

    // =========================================================
    // POST /api/webhook/stripe - Handle Stripe webhooks
    // =========================================================

    @Test
    void testWebhook_InvoicePaid_ActivatesSubscription() throws Exception {
        String payload = createInvoiceEventPayload("cus_test_123", "invoice_payment.paid");

        try (MockedStatic<Webhook> webhookMock = mockStatic(Webhook.class)) {
            Event mockEvent = createMockEvent("invoice_payment.paid", "cus_test_123");
            webhookMock.when(() -> Webhook.constructEvent(anyString(), anyString(), anyString()))
                    .thenReturn(mockEvent);

            mockMvc.perform(post("/api/webhook/stripe")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(payload)
                            .header("Stripe-Signature", "t=1234,v1=fake_signature"))
                    .andExpect(status().isOk())
                    .andExpect(content().string("Webhook received"));

            User updated = userRepo.findByStripeCustomerId("cus_test_123").orElseThrow();
            assertThat(updated.getSubscriptionStatus()).isEqualTo(SubscriptionStatus.ACTIVE);
            assertThat(updated.getGotchaCoins()).isEqualTo(300L);
        }
    }

    @Test
    void testWebhook_InvoicePaymentFailed_DeactivatesSubscription() throws Exception {
        // First activate the user
        testUser.setSubscriptionStatus(SubscriptionStatus.ACTIVE);
        userRepo.save(testUser);

        String payload = createInvoiceEventPayload("cus_test_123", "invoice_payment.failed");

        try (MockedStatic<Webhook> webhookMock = mockStatic(Webhook.class)) {
            Event mockEvent = createMockEvent("invoice_payment.failed", "cus_test_123");
            webhookMock.when(() -> Webhook.constructEvent(anyString(), anyString(), anyString()))
                    .thenReturn(mockEvent);

            mockMvc.perform(post("/api/webhook/stripe")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(payload)
                            .header("Stripe-Signature", "t=1234,v1=fake_signature"))
                    .andExpect(status().isOk());

            User updated = userRepo.findByStripeCustomerId("cus_test_123").orElseThrow();
            assertThat(updated.getSubscriptionStatus()).isEqualTo(SubscriptionStatus.INACTIVE);
        }
    }

    @Test
    void testWebhook_SubscriptionDeleted_DeactivatesSubscription() throws Exception {
        testUser.setSubscriptionStatus(SubscriptionStatus.ACTIVE);
        userRepo.save(testUser);

        String payload = createInvoiceEventPayload("cus_test_123", "customer.subscription.deleted");

        try (MockedStatic<Webhook> webhookMock = mockStatic(Webhook.class)) {
            Event mockEvent = createMockEvent("customer.subscription.deleted", "cus_test_123");
            webhookMock.when(() -> Webhook.constructEvent(anyString(), anyString(), anyString()))
                    .thenReturn(mockEvent);

            mockMvc.perform(post("/api/webhook/stripe")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(payload)
                            .header("Stripe-Signature", "t=1234,v1=fake_signature"))
                    .andExpect(status().isOk());

            User updated = userRepo.findByStripeCustomerId("cus_test_123").orElseThrow();
            assertThat(updated.getSubscriptionStatus()).isEqualTo(SubscriptionStatus.INACTIVE);
        }
    }

    @Test
    void testWebhook_InvalidSignature_ReturnsBadRequest() throws Exception {
        String payload = "{\"type\": \"invoice.paid\"}";

        try (MockedStatic<Webhook> webhookMock = mockStatic(Webhook.class)) {
            webhookMock.when(() -> Webhook.constructEvent(anyString(), anyString(), anyString()))
                    .thenThrow(new com.stripe.exception.SignatureVerificationException(
                            "Invalid signature", "sig_header"));

            mockMvc.perform(post("/api/webhook/stripe")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(payload)
                            .header("Stripe-Signature", "t=1234,v1=invalid"))
                    .andExpect(status().isBadRequest())
                    .andExpect(content().string("Invalid signature"));
        }
    }

    @Test
    void testWebhook_UnknownCustomer_NoError() throws Exception {
        String payload = createInvoiceEventPayload("cus_unknown_999", "invoice_payment.paid");

        try (MockedStatic<Webhook> webhookMock = mockStatic(Webhook.class)) {
            Event mockEvent = createMockEvent("invoice_payment.paid", "cus_unknown_999");
            webhookMock.when(() -> Webhook.constructEvent(anyString(), anyString(), anyString()))
                    .thenReturn(mockEvent);

            mockMvc.perform(post("/api/webhook/stripe")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(payload)
                            .header("Stripe-Signature", "t=1234,v1=fake_signature"))
                    .andExpect(status().isOk())
                    .andExpect(content().string("Webhook received"));

            // Original user should remain unchanged
            User unchanged = userRepo.findByStripeCustomerId("cus_test_123").orElseThrow();
            assertThat(unchanged.getSubscriptionStatus()).isEqualTo(SubscriptionStatus.INACTIVE);
        }
    }

    @Test
    void testWebhook_NoSignatureHeader_ReturnsBadRequest() throws Exception {
        mockMvc.perform(post("/api/webhook/stripe")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"type\": \"invoice.paid\"}"))
                .andExpect(status().isBadRequest());
    }

    // =========================================================
    // Helper methods
    // =========================================================

    @SuppressWarnings("unchecked")
    private Event createMockEvent(String eventType, String customerId) {
        Event event = mock(Event.class);
        when(event.getType()).thenReturn(eventType);
        when(event.getId()).thenReturn("evt_test_123");

        // Mock event.getData().getObject().toJson() chain
        StripeObject stripeObject = mock(StripeObject.class);
        when(stripeObject.toJson()).thenReturn("{\"customer\": \"" + customerId + "\"}");

        Event.Data eventData = mock(Event.Data.class);
        when(eventData.getObject()).thenReturn(stripeObject);
        when(event.getData()).thenReturn(eventData);

        return event;
    }

    private String createInvoiceEventPayload(String customerId, String eventType) {
        return """
                {
                    "id": "evt_test_123",
                    "object": "event",
                    "type": "%s",
                    "data": {
                        "object": {
                            "id": "in_test_123",
                            "object": "invoice",
                            "customer": "%s",
                            "amount_paid": 1500,
                            "currency": "eur",
                            "status": "paid"
                        }
                    }
                }
                """.formatted(eventType, customerId);
    }
}
