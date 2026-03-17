package com.gotcha.gotcha_api.model.dto;

import jakarta.validation.constraints.*;

public record UpdateRSVPRequest(
        @NotBlank(message = "RSVP name is required")
        @Size(max = 255, message = "RSVP name must be less than 255 characters")
        String rsvpName,

        @NotBlank(message = "RSVP email is required")
        @Email(message = "Invalid email format")
        @Size(max = 255, message = "Email must be less than 255 characters")
        String rsvpEmail,

        @NotNull(message = "Guest number is required")
        @Min(value = 1, message = "Guest number must be at least 1")
        @Max(value = 100, message = "Guest number must not exceed 100")
        Long guestNumber
) {
}
