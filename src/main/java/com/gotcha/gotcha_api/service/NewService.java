package com.gotcha.gotcha_api.service;

import com.gotcha.gotcha_api.model.StaticContent;
import com.gotcha.gotcha_api.model.dto.EventResponse;
import com.gotcha.gotcha_api.model.dto.NewsResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NewService {

    @Autowired
    EventService eventService;

    @Autowired
    StaticContentService staticContentService;

    public ResponseEntity<NewsResponse> getAllNews() {
        List<EventResponse> events = eventService.getAllEvents();
        List<StaticContent> staticContents =  staticContentService.getAllSections();
        NewsResponse response = new NewsResponse(
                events,
                staticContents
        );

        return new ResponseEntity(response, HttpStatus.OK);


    }
}
