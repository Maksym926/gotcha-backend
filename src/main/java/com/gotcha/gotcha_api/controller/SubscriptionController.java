package com.gotcha.gotcha_api.controller;

import com.gotcha.gotcha_api.model.UserPrincipal;
import com.gotcha.gotcha_api.service.SubscriptionService;
import com.stripe.exception.StripeException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/member/subscription")
public class SubscriptionController {

    @Autowired
    private SubscriptionService subscriptionService;

    @PostMapping
    public ResponseEntity<Map<String, String>> subscribe(
            @AuthenticationPrincipal UserPrincipal userPrincipal) throws StripeException {

        String clientSecret = subscriptionService.subscribe(userPrincipal.getUser());
        return ResponseEntity.ok(Map.of("clientSecret", clientSecret));
    }

    @DeleteMapping
    public ResponseEntity<String> cancelSubscription(
            @AuthenticationPrincipal UserPrincipal userPrincipal) throws StripeException {

        subscriptionService.cancelSubscription(userPrincipal.getUser());
        return ResponseEntity.ok("Subscription cancelled successfully");
    }
}
