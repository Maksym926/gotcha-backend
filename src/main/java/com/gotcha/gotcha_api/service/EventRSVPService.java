package com.gotcha.gotcha_api.service;

import com.gotcha.gotcha_api.model.Event;
import com.gotcha.gotcha_api.model.EventRSVP;
import com.gotcha.gotcha_api.model.User;
import com.gotcha.gotcha_api.model.UserPrincipal;
import com.gotcha.gotcha_api.model.dto.RSVPRequest;
import com.gotcha.gotcha_api.repo.EventRSVPRepo;
import com.gotcha.gotcha_api.repo.UserRepo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class EventRSVPService {

    @Autowired
    EventService eventService;

    @Autowired
    UserService userService;

    @Autowired
    EventRSVPRepo eventRSVPRepo;

    public EventRSVP submitRSVP(RSVPRequest rsvpRequest, UserPrincipal userPrincipal) {
        log.info("Submitting RSVP");
        EventRSVP eventRSVP = mapToEventRSVP(rsvpRequest, userPrincipal);
        return eventRSVPRepo.save(eventRSVP);

    }
    private EventRSVP mapToEventRSVP(RSVPRequest rsvpRequest, UserPrincipal userPrincipal){
        Event event = eventService.getEventById(rsvpRequest.eventId());
        log.info("User email: " + userPrincipal.getUsername() + " ");
        User user = userService.getUserByEmail(userPrincipal.getUsername());
        EventRSVP eventRSVP = new EventRSVP();
        eventRSVP.setEvent(event);
        eventRSVP.setUser(user);
        eventRSVP.setRsvpName(rsvpRequest.rsvpName());
        eventRSVP.setRsvpEmail(rsvpRequest.rsvpEmail());
        eventRSVP.setGuests(rsvpRequest.guestNumber());
        return eventRSVP;
    }
}
