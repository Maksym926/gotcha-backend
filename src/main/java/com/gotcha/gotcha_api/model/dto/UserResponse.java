package com.gotcha.gotcha_api.model.dto;

import com.gotcha.gotcha_api.enums.AccountStatus;
import com.gotcha.gotcha_api.enums.Role;
import com.gotcha.gotcha_api.enums.SubscriptionStatus;

public record UserResponse(
        Long userId,
        String username,
        String email,
        AccountStatus status,
        Long gotchaCoins,
        String profilePictureKey,
        Role role,
        String mood,
        SubscriptionStatus subscriptionStatus,
        boolean cancelAtPeriodEnd,
        String gotchaFavDrink

) {
}
