package com.gotcha.gotcha_api.model.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record ProductSearchParameter(

        @Size(max = 255, message = "keyword must be less than 255 characters")
        String keyword,

        @Size(max = 255, message = "brand must be less than 255 characters")
        String brand,

        @Size(max = 100, message = "category must be less than 100 characters")
        String category,

        @DecimalMin(value = "0.0", inclusive = true, message = "minPrice must be 0 or greater")
        @Digits(integer = 10, fraction = 2, message = "minPrice must have at most 2 decimal places")
        BigDecimal minPrice,

        @DecimalMin(value = "0.0", inclusive = true, message = "maxPrice must be 0 or greater")
        @Digits(integer = 10, fraction = 2, message = "maxPrice must have at most 2 decimal places")
        BigDecimal maxPrice,

        Boolean productAvailable
) {
}
