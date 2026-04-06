package com.gotcha.gotcha_api.model.dto;

public record AuthResponse(
        String accessToken,
        Long userId
) {
}
