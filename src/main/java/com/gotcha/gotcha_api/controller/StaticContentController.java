package com.gotcha.gotcha_api.controller;

import com.gotcha.gotcha_api.model.StaticContent;
import com.gotcha.gotcha_api.service.StaticContentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api")
@Slf4j
@Tag(name = "Static Content", description = "CMS-style content blocks with optional primary and secondary images")
public class StaticContentController {

    @Autowired
    StaticContentService staticContentService;



    @GetMapping("/member/static-content/{sectionKey}")
    public ResponseEntity<StaticContent> getSection(@PathVariable String sectionKey){

        StaticContent content = staticContentService.getSectionBySectionKey(sectionKey);
        if(content != null){
            return new ResponseEntity<>(content, HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @GetMapping("/admin/static-content")
    public ResponseEntity<List<StaticContent>> getSections(){
        List<StaticContent> staticContent = staticContentService.getAllSections();
        if(!staticContent.isEmpty()){
            return new ResponseEntity<>(staticContent, HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }
    @PutMapping("/admin/static-content/{sectionKey}")
    public ResponseEntity<StaticContent> updateSection(
            @PathVariable String sectionKey,
            @RequestParam("title") String title,
            @RequestParam(value = "description", required = false) String description,
            @RequestPart(value = "imageFile", required = false) MultipartFile imageFile,
            @RequestPart(value = "secondaryImageFile", required = false) MultipartFile secondaryImageFile){
        try{
            log.info("Updating the static content in DB(Controller layer): sectionKey={}", sectionKey);
            StaticContent updatedContent = staticContentService.updateStaticContent(sectionKey, title, description, imageFile, secondaryImageFile);
            return new ResponseEntity<>(updatedContent, HttpStatus.OK);
        }catch (Exception e){
            log.error("Error while uploading to S3", e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

}
