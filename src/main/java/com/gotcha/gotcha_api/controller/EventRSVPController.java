package com.gotcha.gotcha_api.controller;


import com.gotcha.gotcha_api.model.EventRSVP;
import com.gotcha.gotcha_api.model.UserPrincipal;
import com.gotcha.gotcha_api.model.dto.RSVPRequest;
import com.gotcha.gotcha_api.model.dto.UpdateRSVPRequest;
import com.gotcha.gotcha_api.service.EventRSVPService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@CrossOrigin
public class EventRSVPController {

    @Autowired
    EventRSVPService eventRSVPService;


    @PostMapping("/member/event/rsvp")
    public ResponseEntity<String> submitRSVP(@RequestBody RSVPRequest rsvpRequest, @AuthenticationPrincipal UserPrincipal userPrincipal){
        EventRSVP eventRSVP = eventRSVPService.submitRSVP(rsvpRequest, userPrincipal);
        return new ResponseEntity<>("RSVP submitted successfully", HttpStatus.OK);
    }
    @PutMapping("/member/event/rsvp/{event_id}")
    public ResponseEntity<String> updateRSVP(@PathVariable Long rsvp_id, @RequestBody UpdateRSVPRequest updateRsvpRequest){
        EventRSVP upadetedEventRSVP = eventRSVPService.updateRSVP(rsvp_id, updateRsvpRequest);
        return new ResponseEntity<>("RSVP updated successfully", HttpStatus.OK);
    }

    @GetMapping("/admin/event/rsvp")
    public ResponseEntity<List<EventRSVP>> getAllRSVP(){
        return new ResponseEntity<>(eventRSVPService.getAllRSVP(), HttpStatus.OK);
    }
    @GetMapping("/member/event/rsvp/{rsvp_id}")
    public ResponseEntity<EventRSVP> getRSVPById(@PathVariable Long rsvp_id){
        return new ResponseEntity<>(eventRSVPService.getRSVPById(rsvp_id), HttpStatus.OK);

    }

    @DeleteMapping("/member/event/rsvp/{event_id}")
    public ResponseEntity<String> deleteRSVP(@PathVariable Long event_id){
        eventRSVPService.deleteRSVPById(event_id);
        return new ResponseEntity<>("RSVP deleted successfully", HttpStatus.OK);
    }
}
