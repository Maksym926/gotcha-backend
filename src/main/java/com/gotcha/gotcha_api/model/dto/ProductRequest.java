package com.gotcha.gotcha_api.model.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public record ProductRequest(
        @NotBlank(message = "name is required")
        @Size(max = 255, message = "name must be less than 255 characters")
        String name,

        @NotBlank(message = "description is required")
        @Size(max = 1000, message = "description must be less than 1000 characters")
        String description,

        @NotBlank(message = "brand is required")
        @Size(max = 255, message = "brand must be less than 255 characters")
        String brand,

        @NotNull(message = "price is required")
        @DecimalMin(value = "0.0", inclusive = false, message = "price must be greater than 0")
        @Digits(integer = 10, fraction = 2, message = "price must have at most 2 decimal places")
        BigDecimal price,

        @NotBlank(message = "category is required")
        @Size(max = 100, message = "category must be less than 100 characters")
        String category,

        boolean productAvailable,

        @NotNull(message = "stock quantity is required")
        @Min(value = 0, message = "stock quantity must be 0 or greater")
        Long stockQuantity,

        String imageUrl
) {
}
