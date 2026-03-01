package com.gotcha.gotcha_api.service;

import com.gotcha.gotcha_api.exception.custom.ImageGenerationException;
import com.gotcha.gotcha_api.model.User;
import com.gotcha.gotcha_api.model.UserPrincipal;
import com.gotcha.gotcha_api.model.dto.ProfileResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ProfileService {

    @Autowired
    UserService userService;

    @Autowired
    S3Service s3Service;

    public ProfileResponse getProfileInfo(UserPrincipal userPrincipal) {
        User user = userService.getUserByEmail(userPrincipal.getUsername());
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
}
