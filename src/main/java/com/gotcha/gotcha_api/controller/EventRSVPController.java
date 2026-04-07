package com.gotcha.gotcha_api.controller;


import com.gotcha.gotcha_api.model.EventRSVP;
import com.gotcha.gotcha_api.model.UserPrincipal;
import com.gotcha.gotcha_api.model.dto.RSVPRequest;
import com.gotcha.gotcha_api.model.dto.UpdateRSVPRequest;
import com.gotcha.gotcha_api.service.EventRSVPService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Event RSVP", description = "RSVP management for events")
public class EventRSVPController {

    @Autowired
    EventRSVPService eventRSVPService;


    // member

    @Operation(summary = "Submit RSVP", description = "Create a new RSVP for an event. User is identified from the JWT token.")
    @PostMapping("/member/event/rsvp")
    public ResponseEntity<String> submitRSVP(@Valid @RequestBody RSVPRequest rsvpRequest, @AuthenticationPrincipal UserPrincipal userPrincipal){
        eventRSVPService.submitRSVP(rsvpRequest, userPrincipal);
        return new ResponseEntity<>("RSVP submitted successfully", HttpStatus.OK);
    }
    // member — current user's RSVPs

    @Operation(summary = "Get my RSVPs", description = "Returns all RSVPs for the currently authenticated user. No user ID needed — identity is extracted from the JWT token.")
    @GetMapping("/member/event/rsvp/me")
    public ResponseEntity<Page<EventRSVP>> getMyRSVPs(@AuthenticationPrincipal UserPrincipal userPrincipal,
                                                      @PageableDefault(size = 20, sort = "rsvpId", direction = Sort.Direction.DESC) Pageable pageable) {
        Long userId = userPrincipal.getUser().getUserId();
        return new ResponseEntity<>(eventRSVPService.getRSVPByUserId(userId, pageable), HttpStatus.OK);
    }

    @Operation(summary = "Update my RSVP", description = "Update a specific RSVP for the current user. The RSVP is identified by eventId in the request body.")
    @PutMapping("/member/event/rsvp/me")
    public ResponseEntity<String> updateMyRSVP(@AuthenticationPrincipal UserPrincipal userPrincipal,
                                               @Valid @RequestBody UpdateRSVPRequest updateRsvpRequest) {
        Long userId = userPrincipal.getUser().getUserId();
        eventRSVPService.updateRSVPByUserIdAndEventId(userId, updateRsvpRequest);
        return new ResponseEntity<>("RSVP updated successfully", HttpStatus.OK);
    }

    @Operation(summary = "Delete all my RSVPs", description = "Deletes all RSVPs for the currently authenticated user.")
    @DeleteMapping("/member/event/rsvp/me")
    public ResponseEntity<String> deleteAllMyRSVPs(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        Long userId = userPrincipal.getUser().getUserId();
        eventRSVPService.deleteAllRSVPsByUserId(userId);
        return new ResponseEntity<>("All RSVPs deleted successfully", HttpStatus.OK);
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
