package com.gotcha.gotcha_api.service;

import com.gotcha.gotcha_api.model.StaticContent;
import com.gotcha.gotcha_api.model.dto.EventResponse;
import com.gotcha.gotcha_api.model.dto.NewsResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public class NewService {

    @Autowired
    EventService eventService;

    @Autowired
    StaticContentService staticContentService;

    public ResponseEntity<NewsResponse> getAllNews(@PageableDefault(size = 20, sort = "releaseDate", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<EventResponse> events = eventService.getAllEvents(pageable);
        Page<StaticContent> staticContents = staticContentService.getAllSections(pageable);
        NewsResponse response = new NewsResponse(
                events,
                staticContents
        );

        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
