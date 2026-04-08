package com.gotcha.gotcha_api.controller;

import com.gotcha.gotcha_api.enums.SubscriptionStatus;
import com.gotcha.gotcha_api.model.User;
import com.gotcha.gotcha_api.repo.UserRepo;
import com.stripe.exception.SignatureVerificationException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.stripe.model.Event;
import com.stripe.net.Webhook;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/webhook")

public class WebhookController {

    private static final Logger log = LoggerFactory.getLogger(WebhookController.class);

    @Autowired
    private UserRepo userRepo;

    @Value("${stripe.webhook.secret}")
    private String webhookSecret;

    @PostMapping("/stripe")
    public ResponseEntity<String> handleStripeWebhook(
            @RequestBody String payload,
            @RequestHeader("Stripe-Signature") String sigHeader) {

        Event event;

        // Verify the webhook signature to ensure it's from Stripe
        try {
            event = Webhook.constructEvent(payload, sigHeader, webhookSecret);
        } catch (SignatureVerificationException e) {
            log.error("Webhook signature verification failed: {}", e.getMessage());
            return ResponseEntity.badRequest().body("Invalid signature");
        }

        log.info("Received Stripe webhook event: type={}, id={}", event.getType(), event.getId());

        // Extract customer ID from the event's raw JSON (works across all API versions)
        String customerId = extractCustomerFromEvent(event);

        // Handle Stripe events
        switch (event.getType()) {

            case "invoice.paid", "invoice_payment.paid" -> {
                if (customerId != null) {
                    log.info("invoice.paid: activating subscription for customer={}", customerId);
                    activateSubscription(customerId);
                } else {
                    log.warn("invoice.paid: could not extract customer ID from event");
                }
            }

            case "invoice.payment_failed", "invoice_payment.failed" -> {
                if (customerId != null) {
                    log.info("invoice.payment_failed: deactivating subscription for customer={}", customerId);
                    deactivateSubscription(customerId);
                } else {
                    log.warn("invoice.payment_failed: could not extract customer ID from event");
                }
            }

            case "customer.subscription.deleted" -> {
                if (customerId != null) {
                    log.info("subscription.deleted: deactivating subscription for customer={}", customerId);
                    deactivateSubscription(customerId);
                } else {
                    log.warn("customer.subscription.deleted: could not extract customer ID from event");
                }
            }

            default -> log.info("Unhandled event type: {}", event.getType());
        }

        return ResponseEntity.ok("Webhook received");
    }

    private String extractCustomerFromEvent(Event event) {
        try {
            String json = event.getData().getObject().toJson();
            ObjectMapper mapper = new ObjectMapper();
            JsonNode jsonNode = mapper.readTree(json);
            if (jsonNode.has("customer")) {
                return jsonNode.get("customer").asText();
            }
        } catch (Exception e) {
            log.error("Failed to extract customer from event {}: {}", event.getId(), e.getMessage());
        }
        return null;
    }

    private void activateSubscription(String stripeCustomerId) {
        Optional<User> userOpt = userRepo.findByStripeCustomerId(stripeCustomerId);
        userOpt.ifPresent(user -> {
            user.setSubscriptionStatus(SubscriptionStatus.ACTIVE);
            user.setGotchaCoins(user.getGotchaCoins() + 300L);;
            userRepo.save(user);
        });
    }

    private void deactivateSubscription(String stripeCustomerId) {
        Optional<User> userOpt = userRepo.findByStripeCustomerId(stripeCustomerId);
        userOpt.ifPresent(user -> {
            user.setSubscriptionStatus(SubscriptionStatus.INACTIVE);
            userRepo.save(user);
        });
    }
}
