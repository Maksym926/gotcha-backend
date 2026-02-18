package com.gotcha.gotcha_api.model.dto;

import com.gotcha.gotcha_api.enums.Role;

public record LoginRequest(
        String email,
        String password,
        Role role
) {
}
