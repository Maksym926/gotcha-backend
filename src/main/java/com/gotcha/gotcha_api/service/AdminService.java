package com.gotcha.gotcha_api.service;

import com.gotcha.gotcha_api.exception.custom.UserNotFoundException;
import com.gotcha.gotcha_api.model.User;
import com.gotcha.gotcha_api.repo.UserRepo;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

public class AdminService {

    @Autowired
    UserRepo userRepo;

    public List<User> getAllUsers() {
        return userRepo.findAll();
    }
    public User getUserByID(Long id) {
        return userRepo.findById(id).orElseThrow(
                () -> new UserNotFoundException("User with ID " + id + " not found")
        );
    }
    public void deleteUserByID(Long id) {
        User user = getUserByID(id);
        userRepo.delete(user);

    }
}
