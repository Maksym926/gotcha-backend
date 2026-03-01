package com.gotcha.gotcha_api.service;

import com.gotcha.gotcha_api.exception.custom.EventNotFoundException;
import com.gotcha.gotcha_api.exception.custom.ImageFileNotFoundException;
import com.gotcha.gotcha_api.exception.custom.ImageGenerationException;
import com.gotcha.gotcha_api.model.Event;
import com.gotcha.gotcha_api.model.EventRSVP;
import com.gotcha.gotcha_api.model.User;
import com.gotcha.gotcha_api.model.UserPrincipal;
import com.gotcha.gotcha_api.model.dto.EventResponse;
import com.gotcha.gotcha_api.model.dto.RSVPRequest;
import com.gotcha.gotcha_api.repo.EventRepo;
import com.gotcha.gotcha_api.repo.UserRepo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;


import java.io.IOException;
import java.util.List;

@Service
@Slf4j
public class EventService {


    @Autowired
    EventRepo eventRepo;

    @Autowired
    S3Service s3Service;

    @Autowired
    UserRepo userRepo;

    public List<EventResponse> getAllEvents() {
        List<Event> events = eventRepo.findAll();

        log.info("Getting events from DB: " + events.size());

        return events.stream().map(event -> {

            String signedUrl = null;
            if (event.getImageKey() != null) {
                signedUrl = s3Service.generateSignedUrl(event.getImageKey());
            }
            return  new EventResponse(
                    event.getEventId(),
                    event.getTitle(),
                    event.getLocation(),
                    signedUrl
            );

        }).toList();
    }

    public Event addOrUpdateEvent(Event event, MultipartFile file) throws IOException {
        if (file != null && !file.isEmpty()) {
            try{
                log.info("Uploading image to S3: " + file.getOriginalFilename());
                String imageKey = s3Service.uploadFile(file, "events-img");
                event.setImageKey(imageKey);
            }catch (IOException ex){
                throw new ImageFileNotFoundException("Image file not found", ex);
            }
        }

        return eventRepo.save(event);
    }

    public Event getEventById(Long id) {
        Event event = eventRepo.findById(id)
                .orElseThrow(() -> new EventNotFoundException("Event not found with id: " + id ));

        String signedUrl = s3Service.generateSignedUrl(event.getImageKey());
        if(signedUrl == null)
            throw new ImageGenerationException("Image generation failed");
        event.setImageKey(signedUrl);

        return event;
    }

    public void deleteEvent(Long id) {
        eventRepo.deleteById(id);
    }


}
