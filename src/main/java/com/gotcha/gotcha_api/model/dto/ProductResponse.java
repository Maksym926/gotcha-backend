package com.gotcha.gotcha_api.model.dto;

public record ProductResponse(
        String name,
        Long price,
        String imageUrl
) {

}
