package com.gotcha.gotcha_api.controller;

import com.gotcha.gotcha_api.model.Event;
import com.gotcha.gotcha_api.model.dto.EventResponse;
import com.gotcha.gotcha_api.service.EventService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
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

    @GetMapping("/event")
    public ResponseEntity<List<EventResponse>> getEvents(){
        List<EventResponse> events = eventService.getAllEvents();
        return new ResponseEntity<>(events, HttpStatus.OK);
    }
    @GetMapping("/event/{id}")
    public ResponseEntity<Event> getEventById(@PathVariable Long id){
        Event event = eventService.getEventById(id);
        if(event != null){
            return new ResponseEntity<>(event, HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @PostMapping("/event")
    public ResponseEntity<Event> addEvent(@RequestPart("event") Event event, @RequestPart("imageFile") MultipartFile imageFile){
        try{
            log.info("Adding the event to DB(Controller layer): " + event);
            Event savedEvent = eventService.addOrUpdateEvent(event, imageFile);
            return new ResponseEntity<>(savedEvent, HttpStatus.CREATED);
        }catch (Exception e){
            log.error("Error while uploading to S3", e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    @PutMapping("/event/{id}")
    public ResponseEntity<Event> updateEvent(@PathVariable Long id, @RequestPart("event") Event event, @RequestPart("imageFile") MultipartFile imageFile){
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
    @DeleteMapping("/event/{id}")
    public ResponseEntity<String> deleteEvent(@PathVariable Long id){
        Event event = eventService.getEventById(id);
        if(event != null){
            eventService.deleteEvent(id);
            return new ResponseEntity<>("Deleted", HttpStatus.OK);
        }
        return new ResponseEntity<>("Event not found", HttpStatus.NOT_FOUND);
    }




}
