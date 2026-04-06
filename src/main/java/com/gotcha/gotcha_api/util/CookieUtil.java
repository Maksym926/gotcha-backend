package com.gotcha.gotcha_api.util;

import org.springframework.http.ResponseCookie;

import java.time.Duration;

public class CookieUtil {

    private static final String REFRESH_TOKEN_COOKIE_NAME = "refreshToken";
    private static final String COOKIE_PATH = "/api";
    private static final Duration REFRESH_TOKEN_MAX_AGE = Duration.ofDays(7);

    private CookieUtil() {}

    public static ResponseCookie createRefreshTokenCookie(String tokenValue) {
        return ResponseCookie.from(REFRESH_TOKEN_COOKIE_NAME, tokenValue)
                .httpOnly(true)
                .secure(true)
                .sameSite("None")
                .path(COOKIE_PATH)
                .maxAge(REFRESH_TOKEN_MAX_AGE)
                .build();
    }

    public static ResponseCookie deleteRefreshTokenCookie() {
        return ResponseCookie.from(REFRESH_TOKEN_COOKIE_NAME, "")
                .httpOnly(true)
                .secure(true)
                .sameSite("None")
                .path(COOKIE_PATH)
                .maxAge(0)
                .build();
    }
}
