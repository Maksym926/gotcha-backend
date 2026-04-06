package com.gotcha.gotcha_api.service;

import com.gotcha.gotcha_api.exception.custom.ResourceNotFoundException;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
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


        Event event = eventService.getEventById(rsvpRequest.eventId());
        log.info("User email: " + userPrincipal.getUsername() + " ");
        User user = userPrincipal.getUser();
        EventRSVP eventRSVP = new EventRSVP();
        eventRSVP.setEvent(event);
        eventRSVP.setUser(user);
        eventRSVP.setRsvpName(rsvpRequest.rsvpName());
        eventRSVP.setRsvpEmail(rsvpRequest.rsvpEmail());
        eventRSVP.setGuests(rsvpRequest.guestNumber());
        eventRSVP.setCreatedAt(LocalDateTime.now());
        return eventRSVP;
    }

    public Page<EventRSVP> getAllRSVP(Pageable pageable) {
        return eventRSVPRepo.findAll(pageable);
    }

    public void deleteRSVPById(Long eventId) {
        EventRSVP eventRSVP = getRSVPById(eventId);
        eventRSVPRepo.delete(eventRSVP);
    }

    public EventRSVP getRSVPById(Long rsvpId) {
        return eventRSVPRepo.findById(rsvpId).orElseThrow(
                () -> new ResourceNotFoundException("RSVP not found with id: " + rsvpId)
        );
    }

    public EventRSVP updateRSVP(Long rsvpId, UpdateRSVPRequest updateRsvpRequest) {

        EventRSVP eventRSVP = eventRSVPRepo.findById(rsvpId).orElseThrow(
                () -> new ResourceNotFoundException("RSVP not found with id: " + rsvpId)
        );
        eventRSVP.setRsvpName(updateRsvpRequest.rsvpName());
        eventRSVP.setRsvpEmail(updateRsvpRequest.rsvpEmail());
        eventRSVP.setGuests(updateRsvpRequest.guestNumber());

        return eventRSVPRepo.save(eventRSVP);

    }

    public Page<EventRSVP> getRSVPByUserId(Long userId, Pageable pageable) {
        return eventRSVPRepo.findByUser_UserId(userId, pageable).orElseThrow(
                () -> new ResourceNotFoundException("RSVP not found with user id: " + userId)
        );
    }

    public Page<EventRSVP> getRSVPByEventId(Long eventId, Pageable pageable) {
        return eventRSVPRepo.findByEvent_EventId(eventId, pageable).orElseThrow(
                () -> new ResourceNotFoundException("RSVP not found with event id: " + eventId)
        );
    }

    public EventRSVP updateRSVPByUserIdAndEventId(Long userId, UpdateRSVPRequest updateRsvpRequest) {
        EventRSVP eventRSVP = eventRSVPRepo.findByUser_UserIdAndEvent_EventId(userId, updateRsvpRequest.eventId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "RSVP not found for user id: " + userId + " and event id: " + updateRsvpRequest.eventId()));
        eventRSVP.setRsvpName(updateRsvpRequest.rsvpName());
        eventRSVP.setRsvpEmail(updateRsvpRequest.rsvpEmail());
        eventRSVP.setGuests(updateRsvpRequest.guestNumber());
        return eventRSVPRepo.save(eventRSVP);
    }

    @Transactional
    public void deleteAllRSVPsByUserId(Long userId) {
        eventRSVPRepo.deleteByUser_UserId(userId);
    }
}
