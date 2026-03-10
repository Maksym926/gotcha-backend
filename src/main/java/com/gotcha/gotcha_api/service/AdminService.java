package com.gotcha.gotcha_api.service;

import com.gotcha.gotcha_api.exception.custom.ImageGenerationException;
import com.gotcha.gotcha_api.exception.custom.ResourceNotFoundException;
import com.gotcha.gotcha_api.model.User;
import com.gotcha.gotcha_api.model.dto.UserResponse;
import com.gotcha.gotcha_api.repo.UserRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class AdminService {

    @Autowired
    S3Service s3Service;

    @Autowired
    UserRepo userRepo;

    public List<UserResponse> getAllUsers() {
        List<User> users = userRepo.findAll();

        return users.stream().map(user -> {
                String signedUrl = "";
                if(user.getProfilePictureKey() != null){
                    signedUrl = s3Service.generateSignedUrl(user.getProfilePictureKey());
                    if (signedUrl.isBlank())
                        throw new ImageGenerationException("Image generation failed");


                }
                return new UserResponse(
                        user.getUsername(),
                        user.getEmail(),
                        user.getStatus(),
                        user.getGotchaCoins(),
                        signedUrl,
                        user.getRole(),
                        user.getMood(),
                        user.getSubscriptionStatus(),
                        user.getGotchaFavDrink()
                );


        }).toList();

    }
    public User getUserByID(Long id) {
        return userRepo.findById(id).orElseThrow(
                () -> new ResourceNotFoundException("User", id)
        );
    }
    public void deleteUserByID(Long id) {
        User user = getUserByID(id);
        userRepo.delete(user);

    }
}
