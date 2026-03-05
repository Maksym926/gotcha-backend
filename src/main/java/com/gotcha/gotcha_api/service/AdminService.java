package com.gotcha.gotcha_api.service;

import com.gotcha.gotcha_api.exception.custom.ResourceNotFoundException;
import com.gotcha.gotcha_api.model.User;
import com.gotcha.gotcha_api.repo.UserRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AdminService {

    @Autowired
    UserRepo userRepo;

    public List<User> getAllUsers() {
        return userRepo.findAll();
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
