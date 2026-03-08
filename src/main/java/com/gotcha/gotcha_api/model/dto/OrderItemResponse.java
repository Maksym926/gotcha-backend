package com.gotcha.gotcha_api.model.dto;

public record OrderItemResponse(
    String productName,
    Long quantity,
    Long totalPrice
) {
}
