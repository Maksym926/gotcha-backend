package com.gotcha.gotcha_api.controller;

import com.gotcha.gotcha_api.model.User;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@CrossOrigin
@RequestMapping("/api/admin")
public class AdminController {
    @GetMapping
    public ResponseEntity<List<User>> getAllUsers(){

    }
}
