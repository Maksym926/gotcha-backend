package com.gotcha.gotcha_api.service;

import com.gotcha.gotcha_api.enums.SubscriptionStatus;
import com.gotcha.gotcha_api.exception.custom.RefundNotAllowedException;
import com.gotcha.gotcha_api.model.User;
import com.gotcha.gotcha_api.repo.UserRepo;
import com.stripe.exception.StripeException;
import com.stripe.model.Customer;
import com.stripe.model.Invoice;
import com.stripe.model.InvoiceCollection;
import com.stripe.model.Refund;
import com.stripe.model.Subscription;
import com.stripe.param.CustomerCreateParams;
import com.stripe.param.InvoiceListParams;
import com.stripe.param.RefundCreateParams;
import com.stripe.param.SubscriptionCreateParams;
import com.stripe.param.SubscriptionUpdateParams;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

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
        user.setSubscriptionStatus(SubscriptionStatus.INACTIVE);
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

    public void refundAsMember(User user) throws StripeException {
        refundSubscription(user, true);
    }

    public void refundAsAdmin(User user) throws StripeException {
        refundSubscription(user, false);
    }

    private void refundSubscription(User user, boolean enforceTimeLimit) throws StripeException {
        // Guard: must have an active subscription
        if (user.getStripeSubscriptionId() == null && user.getSubscriptionStatus() == SubscriptionStatus.INACTIVE) {
            throw new RefundNotAllowedException("No active subscription to refund");
        }

        // Retrieve the latest paid invoice for this subscription
        InvoiceListParams invoiceParams = InvoiceListParams.builder()
                .setSubscription(user.getStripeSubscriptionId())
                .setLimit(1L)
                .setStatus(InvoiceListParams.Status.PAID)
                .build();

        InvoiceCollection invoices = Invoice.list(invoiceParams);

        if (invoices.getData().isEmpty()) {
            throw new RefundNotAllowedException("No paid invoice found for this subscription");
        }

        Invoice latestInvoice = invoices.getData().get(0);

        // Check 7-day refund window
        if (enforceTimeLimit) {
            Instant invoiceCreated = Instant.ofEpochSecond(latestInvoice.getCreated());
            Instant sevenDaysAgo = Instant.now().minus(7, ChronoUnit.DAYS);

            if (invoiceCreated.isBefore(sevenDaysAgo)) {
                throw new RefundNotAllowedException("Refund window has expired (7 days from last payment)");
            }
        }

        // Get the charge ID from the invoice
        String chargeId = latestInvoice.getCharge();
        if (chargeId == null) {
            throw new RefundNotAllowedException("No charge found on the latest invoice");
        }

        // Create the refund via Stripe
        RefundCreateParams refundParams = RefundCreateParams.builder()
                .setCharge(chargeId)
                .build();
        Refund.create(refundParams);

        // Cancel subscription immediately
        if (user.getStripeSubscriptionId() != null) {
            Subscription subscription = Subscription.retrieve(user.getStripeSubscriptionId());
            subscription.cancel();
        }

        // Deactivate user and claw back coins
        deactivateAndClawBack(user);
    }

    public void deactivateAndClawBack(User user) {
        user.setSubscriptionStatus(SubscriptionStatus.INACTIVE);
        user.setStripeSubscriptionId(null);
        user.setCancelAtPeriodEnd(false);

        Long currentCoins = user.getGotchaCoins() != null ? user.getGotchaCoins() : 0L;
        user.setGotchaCoins(Math.max(0L, currentCoins - 300L));

        userRepo.save(user);
    }
}
