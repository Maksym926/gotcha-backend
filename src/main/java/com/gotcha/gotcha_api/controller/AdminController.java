package com.gotcha.gotcha_api.controller;

import com.gotcha.gotcha_api.model.User;
import com.gotcha.gotcha_api.model.dto.UserResponse;
import com.gotcha.gotcha_api.repo.UserRepo;
import com.gotcha.gotcha_api.service.AdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin
@RequestMapping("/api/admin")
public class AdminController {

    @Autowired
    AdminService adminService;


    @GetMapping("/users")
    public ResponseEntity<List<UserResponse>> getAllUsers(){
        return new ResponseEntity<>(adminService.getAllUsers(), HttpStatus.OK);
    }
    @GetMapping("/users/{id}")
    public ResponseEntity<User> getUserById(@PathVariable Long id){
        return new ResponseEntity<>(adminService.getUserByID(id), HttpStatus.OK);
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<String> deleteUserById(@PathVariable Long id){
        adminService.deleteUserByID(id);
        return new ResponseEntity<>("User deleted successfully", HttpStatus.OK);
    }
}
