package com.gotcha.gotcha_api.model.dto;

import com.gotcha.gotcha_api.model.StaticContent;

import java.util.List;

public record NewsResponse(
        List<EventResponse> event,
        List<StaticContent> staticContent
) {
}
