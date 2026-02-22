package com.gotcha.gotcha_api.model.dto;

public record RSVPRequest(
        Long eventId,
        String rsvpName,
        String rsvpEmail,
        Long guestNumber

) {
}
