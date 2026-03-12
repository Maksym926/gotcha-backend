package com.gotcha.gotcha_api.model.dto;

import com.gotcha.gotcha_api.model.StaticContent;
import org.springframework.data.domain.Page;

import java.util.List;

public record NewsResponse(
        Page<EventResponse> event,
        List<StaticContent> staticContent
) {
}
