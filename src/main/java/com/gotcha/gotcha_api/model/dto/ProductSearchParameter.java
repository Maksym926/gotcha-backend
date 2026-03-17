package com.gotcha.gotcha_api.model.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

public record ProductSearchParameter(

        @Size(max = 255, message = "keyword must be less than 255 characters")
        String keyword,

        @Size(max = 255, message = "brand must be less than 255 characters")
        String brand,

        @Size(max = 100, message = "category must be less than 100 characters")
        String category,

        @Min(value = 0, message = "minPrice must be 0 or greater")
        Long minPrice,

        @Min(value = 0, message = "maxPrice must be 0 or greater")
        Long maxPrice,

        Boolean productAvailable
) {
}
