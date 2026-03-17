package com.gotcha.gotcha_api.model.dto;

import jakarta.validation.constraints.*;

public record RSVPRequest(
        @NotNull(message = "Event id is required")
        @Positive(message = "Event id must be greater than 0")
        Long eventId,

        @NotBlank(message = "RSVP name is required")
        @Size(max = 255, message = "Name must be less than 255 characters")
        String rsvpName,

        @NotBlank(message = "RSVP email is required")
        @Email(message = "Invalid email format")
        @Size(max = 255, message = "Email must be less than 255 characters")
        String rsvpEmail,

        @NotNull(message = "Guest number is required")
        @Positive(message = "Guest number must be greater than 0")
        Long guestNumber
) {
}
