package com.gotcha.gotcha_api.controller;

import com.gotcha.gotcha_api.model.Event;
import com.gotcha.gotcha_api.model.StaticContent;
import com.gotcha.gotcha_api.service.StaticContentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api")
@CrossOrigin
@Slf4j
public class StaticContentController {

    @Autowired
    StaticContentService staticContentService;

    @GetMapping("/static-content")
    public ResponseEntity<List<StaticContent>> getSections(){
        List<StaticContent> staticContent = staticContentService.getAllSections();
        if(!staticContent.isEmpty()){
            return new ResponseEntity<>(staticContent, HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @GetMapping("/static-content/{sectionKey}")
    public ResponseEntity<StaticContent> getSection(@PathVariable String sectionKey){

        StaticContent content = staticContentService.getSection(sectionKey);
        if(content != null){
            return new ResponseEntity<>(content, HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @PutMapping("/static-content/{sectionKey}")
    public ResponseEntity<StaticContent> updateSection(@PathVariable String sectionKey, @RequestPart("content") StaticContent content, @RequestPart("imageFile") MultipartFile imageFile){
        try{
            log.info("Updating the static content in DB(Controller layer): " + content );
            content.setSectionKey(sectionKey);

            StaticContent updatedContent = staticContentService.updateStaticContent(content, imageFile);

            return new ResponseEntity<>(updatedContent, HttpStatus.OK);
        }catch (Exception e){
            log.error("Error while uploading to S3", e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

}
