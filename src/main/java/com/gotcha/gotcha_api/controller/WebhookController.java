package com.gotcha.gotcha_api.controller;

import com.gotcha.gotcha_api.enums.SubscriptionStatus;
import com.gotcha.gotcha_api.model.User;
import com.gotcha.gotcha_api.repo.UserRepo;
import com.gotcha.gotcha_api.service.SubscriptionService;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.exception.StripeException;
import com.stripe.model.Subscription;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.stripe.model.Event;
import com.stripe.net.Webhook;
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

    @Autowired
    private SubscriptionService subscriptionService;

    @Value("${stripe.webhook.secret}")
    private String webhookSecret;

    @PostMapping("/stripe")
    public ResponseEntity<String> handleStripeWebhook(
            @RequestBody String payload,
            @RequestHeader("Stripe-Signature") String sigHeader) {

        Event event;

        try {
            event = Webhook.constructEvent(payload, sigHeader, webhookSecret);
        } catch (SignatureVerificationException e) {
            log.error("Webhook signature verification failed: {}", e.getMessage());
            return ResponseEntity.badRequest().body("Invalid signature");
        }

        log.info("Received Stripe webhook event: type={}, id={}", event.getType(), event.getId());

        String customerId = extractCustomerFromEvent(event);

        switch (event.getType()) {

            case "invoice.paid" -> {
                if (customerId != null) {
                    log.info("invoice.paid: activating subscription for customer={}", customerId);
                    activateSubscription(customerId);
                } else {
                    log.warn("invoice.paid: could not extract customer ID from event");
                }
            }

            case "invoice.payment_failed" -> {
                if (customerId != null) {
                    log.info("invoice.payment_failed: setting PAST_DUE for customer={}", customerId);
                    setSubscriptionPastDue(customerId);
                } else {
                    log.warn("invoice.payment_failed: could not extract customer ID from event");
                }
            }

            case "customer.subscription.updated" -> {
                if (customerId != null) {
                    log.info("subscription.updated: syncing subscription state for customer={}", customerId);
                    syncSubscriptionState(event, customerId);
                } else {
                    log.warn("customer.subscription.updated: could not extract customer ID from event");
                }
            }

            case "charge.refunded" -> {
                if (customerId != null) {
                    log.info("charge.refunded: handling refund for customer={}", customerId);
                    handleChargeRefunded(customerId);
                } else {
                    log.warn("charge.refunded: could not extract customer ID from event");
                }
            }

            case "customer.subscription.deleted" -> {
                if (customerId != null) {
                    log.info("subscription.deleted: fully deactivating subscription for customer={}", customerId);
                    fullyDeactivateSubscription(customerId);
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

            // Direct "customer" field (invoice, subscription events)
            if (jsonNode.has("customer")) {
                String customer = jsonNode.get("customer").asText();
                if (customer != null && !customer.isEmpty() && !"null".equals(customer)) {
                    return customer;
                }
            }

            // Nested under "invoice" → "customer" (invoice_payment events)
            if (jsonNode.has("invoice") && jsonNode.get("invoice").isObject()) {
                JsonNode invoiceNode = jsonNode.get("invoice");
                if (invoiceNode.has("customer")) {
                    return invoiceNode.get("customer").asText();
                }
            }

            log.warn("No customer field found in event {} payload: {}", event.getId(), json);
        } catch (Exception e) {
            log.error("Failed to extract customer from event {}: {}", event.getId(), e.getMessage());
        }
        return null;
    }

    private void activateSubscription(String stripeCustomerId) {
        Optional<User> userOpt = userRepo.findByStripeCustomerId(stripeCustomerId);
        if (userOpt.isEmpty()) {
            log.warn("No user found for stripeCustomerId={}", stripeCustomerId);
            return;
        }
        User user = userOpt.get();
        user.setSubscriptionStatus(SubscriptionStatus.ACTIVE);
        Long currentCoins = user.getGotchaCoins() != null ? user.getGotchaCoins() : 0L;
        user.setGotchaCoins(currentCoins + 300L);
        userRepo.save(user);
        log.info("User {} activated: status={}, coins={}", user.getUserId(), user.getSubscriptionStatus(), user.getGotchaCoins());
    }

    private void setSubscriptionPastDue(String stripeCustomerId) {
        Optional<User> userOpt = userRepo.findByStripeCustomerId(stripeCustomerId);
        userOpt.ifPresent(user -> {
            if (user.getSubscriptionStatus() == SubscriptionStatus.INACTIVE) {
                log.info("Skipping PAST_DUE for user {} — already INACTIVE", user.getUserId());
                return;
            }
            user.setSubscriptionStatus(SubscriptionStatus.PAST_DUE);
            userRepo.save(user);
        });
    }

    private void syncSubscriptionState(Event event, String stripeCustomerId) {
        try {
            String json = event.getData().getObject().toJson();
            ObjectMapper mapper = new ObjectMapper();
            JsonNode jsonNode = mapper.readTree(json);

            String status = jsonNode.has("status") ? jsonNode.get("status").asText() : null;
            boolean cancelAtPeriodEnd = jsonNode.has("cancel_at_period_end")
                    && jsonNode.get("cancel_at_period_end").asBoolean();

            if (status == null) {
                log.warn("customer.subscription.updated: no status field in event data");
                return;
            }

            Optional<User> userOpt = userRepo.findByStripeCustomerId(stripeCustomerId);
            userOpt.ifPresent(user -> {
                switch (status) {
                    case "active" -> {
                        user.setSubscriptionStatus(SubscriptionStatus.ACTIVE);
                        user.setCancelAtPeriodEnd(cancelAtPeriodEnd);
                    }
                    case "past_due" -> {
                        user.setSubscriptionStatus(SubscriptionStatus.PAST_DUE);
                    }
                    case "canceled", "unpaid", "incomplete_expired" -> {
                        user.setSubscriptionStatus(SubscriptionStatus.INACTIVE);
                        user.setStripeSubscriptionId(null);
                        user.setCancelAtPeriodEnd(false);
                    }
                    default -> log.info("Unhandled subscription status: {}", status);
                }
                userRepo.save(user);
            });
        } catch (Exception e) {
            log.error("Failed to sync subscription state for event {}: {}", event.getId(), e.getMessage());
        }
    }

    private void fullyDeactivateSubscription(String stripeCustomerId) {
        Optional<User> userOpt = userRepo.findByStripeCustomerId(stripeCustomerId);
        userOpt.ifPresent(user -> {
            user.setSubscriptionStatus(SubscriptionStatus.INACTIVE);
            user.setStripeSubscriptionId(null);
            user.setCancelAtPeriodEnd(false);
            userRepo.save(user);
        });
    }

    private void handleChargeRefunded(String stripeCustomerId) {
        Optional<User> userOpt = userRepo.findByStripeCustomerId(stripeCustomerId);
        if (userOpt.isEmpty()) {
            log.warn("charge.refunded: no user found for stripeCustomerId={}", stripeCustomerId);
            return;
        }

        User user = userOpt.get();

        // Idempotency: if already deactivated via API, skip
        if (user.getSubscriptionStatus() == SubscriptionStatus.INACTIVE
                && user.getStripeSubscriptionId() == null) {
            log.info("charge.refunded: user {} already deactivated, skipping", user.getUserId());
            return;
        }

        // Cancel subscription on Stripe if still active
        if (user.getStripeSubscriptionId() != null) {
            try {
                Subscription subscription = Subscription.retrieve(user.getStripeSubscriptionId());
                subscription.cancel();
            } catch (StripeException e) {
                log.error("charge.refunded: failed to cancel subscription for user {}: {}",
                        user.getUserId(), e.getMessage());
            }
        }

        subscriptionService.deactivateAndClawBack(user);
        log.info("charge.refunded: user {} deactivated, coins={}", user.getUserId(), user.getGotchaCoins());
    }
}
