package com.gotcha.gotcha_api.controller;

import com.gotcha.gotcha_api.enums.AccountStatus;
import com.gotcha.gotcha_api.exception.custom.InvalidPasswordResetTokenException;
import com.gotcha.gotcha_api.model.EmailVerificationToken;
import com.gotcha.gotcha_api.model.User;
import com.gotcha.gotcha_api.model.dto.ForgotPasswordRequest;
import com.gotcha.gotcha_api.model.dto.LoginRequest;
import com.gotcha.gotcha_api.model.dto.RegisterRequest;
import com.gotcha.gotcha_api.model.dto.ResetPasswordRequest;
import com.gotcha.gotcha_api.repo.EmailVerificationTokenRepo;
import com.gotcha.gotcha_api.repo.UserRepo;
import com.gotcha.gotcha_api.service.JWTService;
import com.gotcha.gotcha_api.service.PasswordResetService;
import com.gotcha.gotcha_api.service.TokenBlacklistService;
import com.gotcha.gotcha_api.service.UserService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@CrossOrigin
@Slf4j
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JWTService jwtService;

    @Autowired
    private UserService userService;

    @Autowired
    private TokenBlacklistService tokenBlacklistService;

    @Autowired
    private PasswordResetService passwordResetService;

    @Autowired
    private EmailVerificationTokenRepo emailVerificationTokenRepo;

    @Autowired
    private UserRepo userRepo;

    @PostMapping("/register")
    public User registerUser(@Valid @RequestBody RegisterRequest registerRequest){
        return userService.saveUser(registerRequest);
    }

    @PostMapping("/login")
    public String loginUser(@Valid @RequestBody LoginRequest loginRequest){
        Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(loginRequest.email(), loginRequest.password()));
        return jwtService.generateToken(loginRequest.email());
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout(@RequestHeader("Authorization") String authHeader){
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            tokenBlacklistService.blacklist(authHeader.substring(7));
        }
        return ResponseEntity.ok("Logged out successfully");
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<String> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        passwordResetService.createPasswordResetToken(request.email());
        return ResponseEntity.ok("If an account with that email exists, a password reset link has been sent.");
    }

    @PostMapping("/reset-password")
    public ResponseEntity<String> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        passwordResetService.resetPassword(request.token(), request.newPassword());
        return ResponseEntity.ok("Password has been reset successfully.");
    }

    @GetMapping("/verify-email")
    public ResponseEntity<String> verifyEmail(@RequestParam String token) {
        EmailVerificationToken verificationToken = emailVerificationTokenRepo.findByToken(token)
                .orElseThrow(() -> new InvalidPasswordResetTokenException("Invalid verification token"));

        if (verificationToken.isUsed()) {
            throw new InvalidPasswordResetTokenException("Verification token has already been used");
        }

        if (verificationToken.isExpired()) {
            throw new InvalidPasswordResetTokenException("Verification token has expired");
        }

        User user = verificationToken.getUser();
        user.setStatus(AccountStatus.ACTIVE);
        userRepo.save(user);

        verificationToken.setUsed(true);
        emailVerificationTokenRepo.save(verificationToken);

        return ResponseEntity.ok("Email verified successfully. You can now log in.");
    }

}
