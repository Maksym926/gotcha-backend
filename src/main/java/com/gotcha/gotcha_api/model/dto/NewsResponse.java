package com.gotcha.gotcha_api.model.dto;

import com.gotcha.gotcha_api.model.StaticContent;
import org.springframework.data.domain.Page;

public record NewsResponse(
        Page<EventResponse> event,
        Page<StaticContent> staticContent
) {
}
