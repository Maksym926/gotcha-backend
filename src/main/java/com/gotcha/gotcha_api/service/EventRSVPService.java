package com.gotcha.gotcha_api.service;

import com.gotcha.gotcha_api.model.Event;
import com.gotcha.gotcha_api.model.EventRSVP;
import com.gotcha.gotcha_api.model.User;
import com.gotcha.gotcha_api.model.UserPrincipal;
import com.gotcha.gotcha_api.model.dto.RSVPRequest;
import com.gotcha.gotcha_api.model.dto.UpdateRSVPRequest;
import com.gotcha.gotcha_api.repo.EventRSVPRepo;
import com.gotcha.gotcha_api.repo.UserRepo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;

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
        if(rsvpRequest instanceof RSVPRequest){

        }
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

    public List<EventRSVP> getAllRSVP() {
        return eventRSVPRepo.findAll();
    }

    public void deleteRSVPById(Long eventId) {
        eventRSVPRepo.deleteById(eventId);
    }

    public EventRSVP getRSVPById(Long rsvpId) {
        return eventRSVPRepo.findById(rsvpId).orElse(null);
    }

    public EventRSVP updateRSVP(Long rsvpId, UpdateRSVPRequest updateRsvpRequest) {
        EventRSVP eventRSVP = eventRSVPRepo.findById(rsvpId).orElse(null);
        if(updateRsvpRequest.rsvpName() != null){
            eventRSVP.setRsvpName(updateRsvpRequest.rsvpName());
        }
        if(updateRsvpRequest.rsvpEmail() != null){
            eventRSVP.setRsvpEmail(updateRsvpRequest.rsvpEmail());
        }
        if(updateRsvpRequest.guestNumber() != null){
            eventRSVP.setGuests(updateRsvpRequest.guestNumber());
        }
        return eventRSVPRepo.save(eventRSVP);

    }
}
