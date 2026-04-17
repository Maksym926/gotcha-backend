package com.gotcha.gotcha_api.service;

import com.gotcha.gotcha_api.model.User;
import com.gotcha.gotcha_api.repo.UserRepo;
import com.stripe.exception.StripeException;
import com.stripe.model.Customer;
import com.stripe.model.Subscription;
import com.stripe.param.CustomerCreateParams;
import com.stripe.param.SubscriptionCreateParams;
import com.stripe.param.SubscriptionUpdateParams;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class SubscriptionService {

    @Autowired
    private UserRepo userRepo;

    @Value("${stripe.price.id}")
    private String priceId;

    public String subscribe(User user) throws StripeException {

        // Re-subscribe: if user cancelled but period hasn't ended yet, just reactivate
        if (user.getStripeSubscriptionId() != null && user.isCancelAtPeriodEnd()) {
            Subscription subscription = Subscription.retrieve(user.getStripeSubscriptionId());
            SubscriptionUpdateParams params = SubscriptionUpdateParams.builder()
                    .setCancelAtPeriodEnd(false)
                    .build();
            subscription.update(params);

            user.setCancelAtPeriodEnd(false);
            userRepo.save(user);
            return null;
        }

        // Step 1: Create or reuse Stripe Customer
        String customerId = user.getStripeCustomerId();
        if (customerId == null) {
            CustomerCreateParams customerParams = CustomerCreateParams.builder()
                    .setEmail(user.getEmail())
                    .setName(user.getUsername())
                    .build();
            Customer customer = Customer.create(customerParams);
            customerId = customer.getId();
            user.setStripeCustomerId(customerId);
            userRepo.save(user);
        }

        // Step 2: Create Subscription
        SubscriptionCreateParams subscriptionParams = SubscriptionCreateParams.builder()
                .setCustomer(customerId)
                .addItem(SubscriptionCreateParams.Item.builder()
                        .setPrice(priceId)
                        .build())
                .setPaymentBehavior(SubscriptionCreateParams.PaymentBehavior.DEFAULT_INCOMPLETE)
                .addExpand("latest_invoice.payment_intent")
                .build();

        Subscription subscription = Subscription.create(subscriptionParams);

        // Step 3: Save subscription ID and set status to INACTIVE until payment confirmed by webhook
        user.setStripeSubscriptionId(subscription.getId());
        user.setSubscriptionStatus(com.gotcha.gotcha_api.enums.SubscriptionStatus.INACTIVE);
        userRepo.save(user);

        // Step 4: Return clientSecret for frontend to confirm payment
        return subscription.getLatestInvoiceObject()
                .getPaymentIntentObject()
                .getClientSecret();
    }

    public void cancelSubscription(User user) throws StripeException {
        if (user.getStripeSubscriptionId() == null) {
            return;
        }

        Subscription subscription = Subscription.retrieve(user.getStripeSubscriptionId());
        SubscriptionUpdateParams params = SubscriptionUpdateParams.builder()
                .setCancelAtPeriodEnd(true)
                .build();
        subscription.update(params);

        user.setCancelAtPeriodEnd(true);
        userRepo.save(user);
    }
}
