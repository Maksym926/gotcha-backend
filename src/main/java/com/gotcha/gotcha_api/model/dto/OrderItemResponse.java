package com.gotcha.gotcha_api.model.dto;

import java.math.BigDecimal;

public record OrderItemResponse(
    String productName,
    Long quantity,
    BigDecimal totalPrice
) {
}
