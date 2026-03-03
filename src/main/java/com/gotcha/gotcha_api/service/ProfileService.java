package com.gotcha.gotcha_api.service;

import com.gotcha.gotcha_api.exception.custom.ImageFileNotFoundException;
import com.gotcha.gotcha_api.exception.custom.ImageGenerationException;
import com.gotcha.gotcha_api.model.User;
import com.gotcha.gotcha_api.model.UserPrincipal;
import com.gotcha.gotcha_api.model.dto.ProfileRequest;
import com.gotcha.gotcha_api.model.dto.ProfileResponse;
import com.gotcha.gotcha_api.repo.UserRepo;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Service
public class ProfileService {

    @Autowired
    S3Service s3Service;

    @Autowired
    UserRepo userRepo;

    public ProfileResponse getProfileInfo(UserPrincipal userPrincipal) {
        User user = userPrincipal.getUser();
        if(user.getProfilePictureKey()!= null){
            String signedUrl = s3Service.generateSignedUrl(user.getProfilePictureKey());
            if(signedUrl == null)
                throw new ImageGenerationException("Image generation failed");
            user.setProfilePictureKey(signedUrl);
        }
        if(user.getGotchaFavDrinkPictureKey() != null){
            String signedUrl = s3Service.generateSignedUrl(user.getGotchaFavDrinkPictureKey());
            if(signedUrl == null)
                throw new ImageGenerationException("Image generation failed");
            user.setGotchaFavDrinkPictureKey(signedUrl);
        }
        return new ProfileResponse(
                user.getUsername(),
                user.getEmail(),
                user.getGotchaCoins(),
                user.getProfilePictureKey(),
                user.getRsvps(),
                user.getMood(),
                user.getSubscriptionStatus(),
                user.getGotchaFavDrink(),
                user.getGotchaFavDrinkPictureKey()
        );


    }

    public void updateProfileInfo(@Valid ProfileRequest profileRequest, MultipartFile profileImage, UserPrincipal userPrincipal) {
        User user = userPrincipal.getUser();
        if (profileImage != null && !profileImage.isEmpty()) {
            try{
                String imageKey = s3Service.uploadFile(profileImage, "profile/profilePicture");
                user.setProfilePictureKey(imageKey);
            }catch (IOException ex){
                throw new ImageFileNotFoundException("Image file not found", ex);
            }
        }
        if(!profileRequest.username().isBlank())
            user.setUsername(profileRequest.username());
        if(!profileRequest.mood().isBlank())
            user.setMood(profileRequest.mood());
        if(!profileRequest.gotchaFavDrink().isBlank())
            user.setGotchaFavDrink(profileRequest.gotchaFavDrink());

        userRepo.save(user);
    }

    public void deleteProfile(UserPrincipal userPrincipal) {
        User user = userPrincipal.getUser();
        userRepo.delete(user);
    }
}
