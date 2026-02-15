package com.gotcha.gotcha_api.service;

import com.gotcha.gotcha_api.model.Event;
import com.gotcha.gotcha_api.model.dto.EventResponse;
import com.gotcha.gotcha_api.repo.EventRepo;
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
                    event.getDescription(),
                    signedUrl
            );

        }).toList();
    }

    public Event addOrUpdateEvent(Event event, MultipartFile file) throws IOException {
        if (file != null && !file.isEmpty()) {
            log.info("Uploading image to S3: " + file.getOriginalFilename());
            String imageKey = s3Service.uploadFile(file, "events-img");
            event.setImageKey(imageKey);
        }

        return eventRepo.save(event);
    }

    public Event getEventById(Long id) {
        Event event = eventRepo.findById(id).orElse(null);

        if(event != null){
            String signedUrl;
            if(event.getImageKey() != null){
                signedUrl = s3Service.generateSignedUrl(event.getImageKey());
                event.setImageKey(signedUrl);
            }
            return event;
        }
        return null;
    }

    public void deleteEvent(Long id) {
        eventRepo.deleteById(id);
    }
}
