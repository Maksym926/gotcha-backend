package com.gotcha.gotcha_api.model.dto;

public record UpdateRSVPRequest(
        String rsvpName,
        String rsvpEmail,
        Long guestNumber
) {
}
