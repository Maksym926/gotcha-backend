package com.gotcha.gotcha_api.model.dto;

import java.math.BigDecimal;

public record ProductResponse(
        String name,
        BigDecimal price,
        String imageUrl
) {

}
