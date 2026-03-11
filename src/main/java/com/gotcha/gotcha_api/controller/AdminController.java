package com.gotcha.gotcha_api.controller;

import com.gotcha.gotcha_api.model.User;
import com.gotcha.gotcha_api.model.dto.UserResponse;
import com.gotcha.gotcha_api.repo.UserRepo;
import com.gotcha.gotcha_api.service.AdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
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
    public ResponseEntity<Page<UserResponse>> getAllUsers( @PageableDefault(size = 20, sort = "createDate", direction = Sort.Direction.DESC) Pageable pageable){
        return new ResponseEntity<>(adminService.getAllUsers(pageable), HttpStatus.OK);
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
