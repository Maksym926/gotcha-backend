package com.gotcha.gotcha_api.model.dto;

public record EventResponse(
     Long id,
     String title,
     String description,
     String imgUrl
) {
}
