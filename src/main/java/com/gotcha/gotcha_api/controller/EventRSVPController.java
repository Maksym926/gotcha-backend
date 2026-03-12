package com.gotcha.gotcha_api.controller;


import com.gotcha.gotcha_api.model.EventRSVP;
import com.gotcha.gotcha_api.model.UserPrincipal;
import com.gotcha.gotcha_api.model.dto.RSVPRequest;
import com.gotcha.gotcha_api.model.dto.UpdateRSVPRequest;
import com.gotcha.gotcha_api.service.EventRSVPService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
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


    // member

    @PostMapping("/member/event/rsvp")
    public ResponseEntity<String> submitRSVP(@Valid @RequestBody RSVPRequest rsvpRequest, @AuthenticationPrincipal UserPrincipal userPrincipal){
        EventRSVP eventRSVP = eventRSVPService.submitRSVP(rsvpRequest, userPrincipal);
        return new ResponseEntity<>("RSVP submitted successfully", HttpStatus.OK);
    }
    @PutMapping("/member/event/rsvp/{rsvp_id}")
    public ResponseEntity<String> updateRSVP(@Valid @PathVariable("rsvp_id") Long rsvp_id, @RequestBody UpdateRSVPRequest updateRsvpRequest){
        EventRSVP upadetedEventRSVP = eventRSVPService.updateRSVP(rsvp_id, updateRsvpRequest);
        return new ResponseEntity<>("RSVP updated successfully", HttpStatus.OK);
    }
    @GetMapping("/member/event/rsvp/{rsvp_id}")
    public ResponseEntity<EventRSVP> getRSVPById(@PathVariable Long rsvp_id){
        return new ResponseEntity<>(eventRSVPService.getRSVPById(rsvp_id), HttpStatus.OK);

    }
    @DeleteMapping("/member/event/rsvp/{rsvp_id}")
    public ResponseEntity<String> deleteRSVP(@PathVariable Long rsvp_id){

        eventRSVPService.deleteRSVPById(rsvp_id);
        return new ResponseEntity<>("RSVP deleted successfully", HttpStatus.OK);
    }

    //admin

    @GetMapping("/admin/user/{user_id}/rsvp")
    public ResponseEntity<Page<EventRSVP>> getRSVPByUserId(@PathVariable("user_id") Long user_id, @PageableDefault(size = 20, sort = "rsvpId", direction = Sort.Direction.DESC) Pageable pageable){
        Page<EventRSVP> rsvpEvents = eventRSVPService.getRSVPByUserId(user_id, pageable);
        return new ResponseEntity<>(rsvpEvents, HttpStatus.OK);
    }
    @GetMapping("/admin/event/{event_id}/rsvp")
    public ResponseEntity<Page<EventRSVP>> getRSVPByEvenId(@PathVariable("event_id") Long event_id, @PageableDefault(size = 20, sort = "rsvpId", direction = Sort.Direction.DESC) Pageable pageable){
        Page<EventRSVP> rsvpEvents = eventRSVPService.getRSVPByEventId(event_id, pageable);
        return new ResponseEntity<>(rsvpEvents, HttpStatus.OK);
    }
    @GetMapping("/admin/event/rsvp")
    public ResponseEntity<Page<EventRSVP>> getAllRSVP(@PageableDefault(size = 20, sort = "rsvpId", direction = Sort.Direction.DESC) Pageable pageable){
        return new ResponseEntity<>(eventRSVPService.getAllRSVP(pageable), HttpStatus.OK);
    }


}
