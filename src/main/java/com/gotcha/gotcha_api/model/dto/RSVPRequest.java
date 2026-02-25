package com.gotcha.gotcha_api.model.dto;

import jakarta.validation.constraints.*;

public record RSVPRequest(
        @NotBlank(message = "event id is required")
        @Positive(message = "Event id must be greater than 0")
        Long eventId,
        @NotBlank(message = "rsvp name is required")
        @Size(max = 255, message = "name must be less than 255 characters")
        String rsvpName,
        @NotBlank(message = "rsvp email is required")
        @Email(message = "invalid email format")
        @Size(max = 255, message = "email must be less than 255 characters")
        String rsvpEmail,
        @NotNull(message = "guest number is required")
        @Positive(message = "Guest number must be greater than 0")
        Long guestNumber
) {
}
