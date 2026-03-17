package com.gotcha.gotcha_api.model.dto;

import com.gotcha.gotcha_api.enums.OrderStatus;

import java.time.LocalDateTime;
import java.util.List;

public record OrderResponse(
    Long orderId,
    String orderCode,
    OrderStatus status,
    LocalDateTime orderDate,
    Long totalPrice,
    List<OrderItemResponse> items
) {
}
