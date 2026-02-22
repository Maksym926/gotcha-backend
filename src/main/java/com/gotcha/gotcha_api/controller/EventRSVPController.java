package com.gotcha.gotcha_api.controller;


import com.gotcha.gotcha_api.model.EventRSVP;
import com.gotcha.gotcha_api.model.UserPrincipal;
import com.gotcha.gotcha_api.model.dto.RSVPRequest;
import com.gotcha.gotcha_api.service.EventRSVPService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/member/event/rsvp")
@CrossOrigin
public class EventRSVPController {

    @Autowired
    EventRSVPService eventRSVPService;


    @PostMapping
    public ResponseEntity<String> submitRSVP(@RequestBody RSVPRequest rsvpRequest, @AuthenticationPrincipal UserPrincipal userPrincipal){
        EventRSVP eventRSVP = eventRSVPService.submitRSVP(rsvpRequest, userPrincipal);
        return new ResponseEntity<>("RSVP submitted successfully", HttpStatus.OK);
    }
}
