package com.gotcha.gotcha_api.service;

import com.gotcha.gotcha_api.exception.custom.ImageFileNotFoundException;
import com.gotcha.gotcha_api.exception.custom.ResourceNotFoundException;
import com.gotcha.gotcha_api.model.StaticContent;
import com.gotcha.gotcha_api.repo.StaticContentRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Service
public class StaticContentService {

    @Autowired
    StaticContentRepo staticContentRepo;

    @Autowired
    S3Service s3Service;

    public StaticContent getSectionBySectionKey(String sectionKey){
        StaticContent staticContent = staticContentRepo.findBySectionKey(sectionKey).orElseThrow(() ->
                new ResourceNotFoundException("Section " + sectionKey + " not found"));
        String signedUrl;
        signedUrl = s3Service.generateSignedUrl(staticContent.getImageKey());
        staticContent.setImageKey(signedUrl);
        return staticContent;

    }

    public List<StaticContent> getAllSections() {
        List<StaticContent> staticContentList = staticContentRepo.findAll();
        return staticContentList.stream().map(staticContent -> {
            String signedUrl;
            signedUrl = s3Service.generateSignedUrl(staticContent.getImageKey());
            staticContent.setImageKey(signedUrl);
            return staticContent;
        }).toList();

    }

    public StaticContent updateStaticContent(StaticContent content, MultipartFile file) throws IOException {

        StaticContent existingContent = getSectionBySectionKey(content.getSectionKey());

        existingContent.setTitle(content.getTitle());
        existingContent.setDescription(content.getDescription());
        if(file != null && !file.isEmpty()){
            try{
                String imageKey = s3Service.uploadFile(file, "static-content-img");
                existingContent.setImageKey(imageKey);
            }catch (IOException ex){
                throw new ImageFileNotFoundException("Image file not found", ex);
            }

        }
        return staticContentRepo.save(existingContent);





    }
}
