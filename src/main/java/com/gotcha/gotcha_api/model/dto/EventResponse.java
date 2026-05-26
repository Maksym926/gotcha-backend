package com.gotcha.gotcha_api.model.dto;

import java.time.LocalDateTime;

public record EventResponse(
     Long id,
     String title,
     LocalDateTime eventDate,
     String imgUrl
) {
}
