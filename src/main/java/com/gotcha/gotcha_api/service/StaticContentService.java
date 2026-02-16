package com.gotcha.gotcha_api.service;

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

    public StaticContent getSection(String sectionKey) {
        StaticContent staticContent = staticContentRepo.findBySectionKey(sectionKey).orElseThrow(() ->
                new RuntimeException("Not found"));
        String signedUrl;
        if(staticContent.getImageKey() != null){
            signedUrl = s3Service.generateSignedUrl(staticContent.getImageKey());
            staticContent.setImageKey(signedUrl);
        }
        return staticContent;

    }

    public List<StaticContent> getAllSections() {
        List<StaticContent> staticContentList = staticContentRepo.findAll();
        return staticContentList.stream().map(staticContent -> {
            String signedUrl;
            if(staticContent.getImageKey() != null){
                signedUrl = s3Service.generateSignedUrl(staticContent.getImageKey());
                staticContent.setImageKey(signedUrl);
            }
            return staticContent;
        }).toList();

    }

    public StaticContent updateStaticContent(StaticContent content, MultipartFile file) throws IOException {
        if(file != null && !file.isEmpty()){
            String imageKey = s3Service.uploadFile(file, "static-content-img");
            content.setImageKey(imageKey);
            return staticContentRepo.save(content);
        }
        return null;

    }
}
