package com.gotcha.gotcha_api.model.dto;

import com.gotcha.gotcha_api.enums.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record LoginRequest(
        @NotBlank(message = "Email is required")
        @Email(message = "Invalid email format")
        String email,
        @NotBlank(message = "Password is required")

        String password,
        @NotNull(message = "Role is required")
        Role role
) {
}
