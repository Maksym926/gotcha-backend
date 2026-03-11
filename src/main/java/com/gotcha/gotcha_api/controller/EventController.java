package com.gotcha.gotcha_api.controller;

import com.gotcha.gotcha_api.model.Event;
import com.gotcha.gotcha_api.model.EventRSVP;
import com.gotcha.gotcha_api.model.UserPrincipal;
import com.gotcha.gotcha_api.model.dto.EventResponse;
import com.gotcha.gotcha_api.model.dto.RSVPRequest;
import com.gotcha.gotcha_api.service.EventService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api")
@CrossOrigin
@Slf4j
public class EventController {

    @Autowired
    EventService eventService;

    @GetMapping("member/event")
    public ResponseEntity<Page<EventResponse>> getEvents(@PageableDefault(size = 20, sort = "releaseDate", direction = Sort.Direction.DESC) Pageable pageable){
        Page<EventResponse> events = eventService.getAllEvents(pageable);
        return new ResponseEntity<>(events, HttpStatus.OK);
    }

    @GetMapping("member/event/{id}")
    public ResponseEntity<Event> getEventById(@PathVariable Long id){
        Event event = eventService.getEventById(id);
        return new ResponseEntity<>(event, HttpStatus.OK);

    }

    @PostMapping("/admin/event")
    public ResponseEntity<Event> addEvent(@Valid @RequestPart("event") Event event, @RequestPart("imageFile") MultipartFile imageFile){
        try{
            log.info("Adding the event to DB(Controller layer): " + event);
            Event savedEvent = eventService.addOrUpdateEvent(event, imageFile);
            return new ResponseEntity<>(savedEvent, HttpStatus.CREATED);
        }catch (Exception e){
            log.error("Error while uploading to S3", e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping("/admin/event/{id}")
    public ResponseEntity<Event> updateEvent(@PathVariable Long id, @Valid @RequestPart("event") Event event, @RequestPart("imageFile") MultipartFile imageFile){
        try{
            log.info("Updating the event in DB(Controller layer): " + event);
            event.setEventId(id);
            Event savedEvent = eventService.addOrUpdateEvent(event, imageFile);

            return new ResponseEntity<>(savedEvent, HttpStatus.OK);
        }catch (Exception e){
            log.error("Error while uploading to S3", e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping("/admin/event/{id}")
    public ResponseEntity<String> deleteEvent(@PathVariable Long id){
        Event event = eventService.getEventById(id);
        eventService.deleteEvent(id);
        return new ResponseEntity<>("Deleted", HttpStatus.OK);
    }






}
