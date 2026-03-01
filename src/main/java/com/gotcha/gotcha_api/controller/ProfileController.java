package com.gotcha.gotcha_api.controller;

import com.gotcha.gotcha_api.model.UserPrincipal;
import com.gotcha.gotcha_api.model.dto.ProfileResponse;
import com.gotcha.gotcha_api.service.ProfileService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/member")
@CrossOrigin
@Slf4j
public class ProfileController {

    @Autowired
    ProfileService profileService;

    @GetMapping("/profile")
    public ResponseEntity<ProfileResponse> getProfileInfo(@AuthenticationPrincipal UserPrincipal userPrincipal){
        return new ResponseEntity<>(profileService.getProfileInfo(userPrincipal), HttpStatus.OK);
    }
}
