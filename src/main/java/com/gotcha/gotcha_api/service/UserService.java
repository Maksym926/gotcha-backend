package com.gotcha.gotcha_api.service;

import com.gotcha.gotcha_api.enums.AccountStatus;
import com.gotcha.gotcha_api.enums.Role;
import com.gotcha.gotcha_api.exception.custom.DuplicateEmailException;
import com.gotcha.gotcha_api.model.EmailVerificationToken;
import com.gotcha.gotcha_api.model.User;
import com.gotcha.gotcha_api.model.dto.RegisterRequest;
import com.gotcha.gotcha_api.repo.EmailVerificationTokenRepo;
import com.gotcha.gotcha_api.repo.UserRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class UserService {

    @Autowired
    UserRepo userRepo;

    @Autowired
    EmailVerificationTokenRepo emailVerificationTokenRepo;

    @Autowired
    EmailService emailService;

    private BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(12);

    @Transactional
    public User saveUser(RegisterRequest registerRequest) {

        if (userRepo.findByEmail(registerRequest.email()).isPresent()) {
            throw new DuplicateEmailException("Email already exists: " + registerRequest.email());
        }

        User user = mapToUser(registerRequest);
        User savedUser = userRepo.save(user);

//        String token = UUID.randomUUID().toString();
//        EmailVerificationToken verificationToken = new EmailVerificationToken();
//        verificationToken.setToken(token);
//        verificationToken.setUser(savedUser);
//        verificationToken.setExpiryDate(LocalDateTime.now().plusHours(24));
//        verificationToken.setUsed(false);
//        verificationToken.setCreatedAt(LocalDateTime.now());
//        emailVerificationTokenRepo.save(verificationToken);
//
//        emailService.sendVerificationEmail(savedUser.getEmail(), token);

        return savedUser;
    }

    private User mapToUser(RegisterRequest request){

        User user = new User();

        user.setUsername(request.userName());
        user.setEmail(request.email());
        user.setPassword(encoder.encode(request.password()));
        user.setStatus(AccountStatus.ACTIVE);
        user.setRole(Role.MEMBER);

        return user;

    }
}
