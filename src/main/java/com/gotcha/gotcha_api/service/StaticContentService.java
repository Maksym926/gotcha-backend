package com.gotcha.gotcha_api.service;

import com.gotcha.gotcha_api.exception.custom.ImageFileNotFoundException;
import com.gotcha.gotcha_api.exception.custom.ResourceNotFoundException;
import com.gotcha.gotcha_api.model.StaticContent;
import com.gotcha.gotcha_api.repo.StaticContentRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Service
public class StaticContentService {

    @Autowired
    StaticContentRepo staticContentRepo;

    @Autowired
    S3Service s3Service;

    public StaticContent getSectionBySectionKey(String sectionKey){
        StaticContent staticContent = staticContentRepo.findBySectionKey(sectionKey).orElseThrow(() ->
                new ResourceNotFoundException("Section " + sectionKey + " not found"));
        if(staticContent.getImageKey() != null){
            staticContent.setImageKey(s3Service.generateSignedUrl(staticContent.getImageKey()));
        }
        if(staticContent.getSecondaryImageKey() != null){
            staticContent.setSecondaryImageKey(s3Service.generateSignedUrl(staticContent.getSecondaryImageKey()));
        }

        return staticContent;

    }

    public List<StaticContent> getAllSections() {
        List<StaticContent> staticContentPage = staticContentRepo.findAll();
        return staticContentPage.stream().peek(staticContent -> {
            if(staticContent.getImageKey() != null){
                staticContent.setImageKey(s3Service.generateSignedUrl(staticContent.getImageKey()));
            }
            if(staticContent.getSecondaryImageKey() != null){
                staticContent.setSecondaryImageKey(s3Service.generateSignedUrl(staticContent.getSecondaryImageKey()));
            }
        }).toList();
    }

    public StaticContent updateStaticContent(String sectionKey, String title, String description, MultipartFile file, MultipartFile secondaryFile) throws IOException {

        StaticContent existingContent = staticContentRepo.findBySectionKey(sectionKey).orElseThrow(() ->
                new ResourceNotFoundException("Section " + sectionKey + " not found"));

        existingContent.setTitle(title);
        existingContent.setDescription(description);
        if(file != null && !file.isEmpty()){
            try{
                String imageKey = s3Service.uploadFile(file, "static-content-img");
                existingContent.setImageKey(imageKey);
            }catch (IOException ex){
                throw new ImageFileNotFoundException("Image file not found", ex);
            }
        }
        if(secondaryFile != null && !secondaryFile.isEmpty()){
            try{
                String secondaryImageKey = s3Service.uploadFile(secondaryFile, "static-content-img");
                existingContent.setSecondaryImageKey(secondaryImageKey);
            }catch (IOException ex){
                throw new ImageFileNotFoundException("Secondary image file not found", ex);
            }
        }
        return staticContentRepo.save(existingContent);





    }
}
