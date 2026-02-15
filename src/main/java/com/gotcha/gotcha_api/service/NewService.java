package com.gotcha.gotcha_api.service;

import com.gotcha.gotcha_api.model.Event;
import com.gotcha.gotcha_api.model.News;
import com.gotcha.gotcha_api.model.dto.EventResponse;
import org.apache.coyote.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NewService {

    @Autowired
    EventService eventService;

    public ResponseEntity<News> getAllNews() {
        List<EventResponse> events = eventService.getAllEvents();
        News news = new News(events);
        return new ResponseEntity(news, HttpStatus.OK);


    }
}
