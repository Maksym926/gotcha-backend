package com.gotcha.gotcha_api.service;

import com.gotcha.gotcha_api.exception.custom.ImageFileNotFoundException;
import com.gotcha.gotcha_api.exception.custom.ImageGenerationException;
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
        String signedUrl;
        if(staticContent.getImageKey() != null){
            signedUrl = s3Service.generateSignedUrl(staticContent.getImageKey());
            staticContent.setImageKey(signedUrl);
            if(signedUrl.isBlank())
                throw  new ImageGenerationException("Fail to generate an image url");
        }


        return staticContent;

    }

    public List<StaticContent> getAllSections() {
        List<StaticContent> staticContentPage = staticContentRepo.findAll();
        return staticContentPage.stream().peek(staticContent -> {
            String signedUrl;
            signedUrl = s3Service.generateSignedUrl(staticContent.getImageKey());
            if(signedUrl.isBlank())
                throw new ImageGenerationException("Fail to generate an image url");
            staticContent.setImageKey(signedUrl);
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
