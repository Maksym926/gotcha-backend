package com.gotcha.gotcha_api.model.dto;

import jakarta.validation.constraints.Size;

public record ProfileRequest(
    @Size(min = 2, max = 50, message = "Username must be between 2 and 50 characters")
    String username,
    @Size(max = 100, message = "Mood must be under 100 characters")
    String mood,
    @Size(max = 100, message = "Favourite drink must be under 100 characters")
    String gotchaFavDrink
) {
}
