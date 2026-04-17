package com.gotcha.gotcha_api.model.dto;

import com.gotcha.gotcha_api.enums.SubscriptionStatus;
import com.gotcha.gotcha_api.model.EventRSVP;

import java.util.List;

public record ProfileResponse(
        String username,
        String email,
        Long gotchaCoins,
        String profilePictureUrl,
        String mood,
        SubscriptionStatus subscriptionStatus,
        boolean cancelAtPeriodEnd,
        String gotchaFavDrink,
        String gotchaFavDrinkPictureUrl
) {
}
