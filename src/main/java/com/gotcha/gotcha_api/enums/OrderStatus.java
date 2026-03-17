package com.gotcha.gotcha_api.enums;

public enum OrderStatus {
    PENDING,        // order created, not yet confirmed
    CONFIRMED,      // cafe accepted the order
    PREPARING,      // being made in the kitchen
    READY,          // ready for pickup / delivery
    DELIVERED,      // handed to customer
    COMPLETED,      // finished, closed
    CANCELLED       // cancelled by user or cafe
}
