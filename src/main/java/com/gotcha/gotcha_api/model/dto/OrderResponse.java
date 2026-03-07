package com.gotcha.gotcha_api.model.dto;

import com.gotcha.gotcha_api.enums.OrderStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record OrderResponse(
    String orderCode,
    OrderStatus status,
    LocalDateTime orderDate,
    BigDecimal totalPrice,
    List<OrderItemResponse> items

) {
}
