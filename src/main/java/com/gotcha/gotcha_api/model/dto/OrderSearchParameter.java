package com.gotcha.gotcha_api.model.dto;

import com.gotcha.gotcha_api.enums.OrderStatus;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

public record OrderSearchParameter(

        @Size(max = 50, message = "orderCode must be less than 50 characters")
        String orderCode,

        OrderStatus status,

        @Min(value = 0, message = "minPrice must be 0 or greater")
        Long minPrice,

        @Min(value = 0, message = "maxPrice must be 0 or greater")
        Long maxPrice,

        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
        LocalDateTime createDate,

        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
        LocalDateTime updateDate
) {
}
