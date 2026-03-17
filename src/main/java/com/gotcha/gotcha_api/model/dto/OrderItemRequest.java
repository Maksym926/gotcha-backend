package com.gotcha.gotcha_api.model.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record OrderItemRequest(
    @NotNull(message = "Product ID is required")
    Long productId,
    @NotNull(message = "quantity is required")
    @Min(value = 1, message = "quantity should be greater than 0")
    Long quantity
) {
}
