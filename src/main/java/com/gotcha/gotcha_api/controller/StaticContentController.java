package com.gotcha.gotcha_api.controller;

import com.gotcha.gotcha_api.model.StaticContent;
import com.gotcha.gotcha_api.service.StaticContentService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api")
@CrossOrigin
@Slf4j
public class StaticContentController {

    @Autowired
    StaticContentService staticContentService;

    @GetMapping("/member/static-content")
    public ResponseEntity<Page<StaticContent>> getSections(@PageableDefault(size = 20, sort = "id", direction = Sort.Direction.ASC) Pageable pageable){
        Page<StaticContent> staticContent = staticContentService.getAllSections(pageable);
        if(!staticContent.isEmpty()){
            return new ResponseEntity<>(staticContent, HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @GetMapping("/member/static-content/{sectionKey}")
    public ResponseEntity<StaticContent> getSection(@PathVariable String sectionKey){

        StaticContent content = staticContentService.getSectionBySectionKey(sectionKey);
        if(content != null){
            return new ResponseEntity<>(content, HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @PutMapping("/admin/static-content/{sectionKey}")
    public ResponseEntity<StaticContent> updateSection(@PathVariable String sectionKey, @Valid @RequestPart("content") StaticContent content, @RequestPart("imageFile") MultipartFile imageFile){
        try{

            content.setSectionKey(sectionKey);
            log.info("Updating the static content in DB(Controller layer): " + content );
            StaticContent updatedContent = staticContentService.updateStaticContent(content, imageFile);

            return new ResponseEntity<>(updatedContent, HttpStatus.OK);
        }catch (Exception e){
            log.error("Error while uploading to S3", e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

}
