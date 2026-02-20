package com.gotcha.gotcha_api.service;

import com.gotcha.gotcha_api.enums.Role;
import com.gotcha.gotcha_api.model.User;
import com.gotcha.gotcha_api.model.dto.RegisterRequest;
import com.gotcha.gotcha_api.repo.UserRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@Service
public class UserService {

    @Autowired
    UserRepo userRepo;

    private BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(12);

    public User saveUser(RegisterRequest registerRequest) {
        User user = mapToUser(registerRequest);
        return userRepo.save(user);
    }

    private User mapToUser(RegisterRequest request){

        User user = new User();
        user.setUsername(request.userName());
        user.setEmail(request.email());
        user.setPassword(encoder.encode(request.password()));
        user.setRole(Role.MEMBER);

        return user;

    }
}
