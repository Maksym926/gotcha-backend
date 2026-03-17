package com.gotcha.gotcha_api.service;

import com.gotcha.gotcha_api.exception.custom.InvalidPasswordResetTokenException;
import com.gotcha.gotcha_api.model.PasswordResetToken;
import com.gotcha.gotcha_api.model.User;
import com.gotcha.gotcha_api.repo.PasswordResetTokenRepo;
import com.gotcha.gotcha_api.repo.UserRepo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@Slf4j
public class PasswordResetService {

    @Autowired
    private PasswordResetTokenRepo passwordResetTokenRepo;

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private EmailService emailService;

    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(12);

    @Transactional
    public void createPasswordResetToken(String email) {
        Optional<User> userOptional = userRepo.findByEmail(email);

        if (userOptional.isEmpty()) {
            // Do not reveal whether the email exists — prevents enumeration attacks
            log.warn("Password reset requested for non-existent email: {}", email);
            return;
        }

        User user = userOptional.get();

        String token = UUID.randomUUID().toString();

        PasswordResetToken resetToken = new PasswordResetToken();
        resetToken.setToken(token);
        resetToken.setUser(user);
        resetToken.setExpiryDate(LocalDateTime.now().plusMinutes(15));
        resetToken.setUsed(false);
        resetToken.setCreatedAt(LocalDateTime.now());

        passwordResetTokenRepo.save(resetToken);

        emailService.sendPasswordResetEmail(user.getEmail(), token);

        log.info("Password reset token created for user: {}", user.getUserId());
    }

    @Transactional
    public void resetPassword(String token, String newPassword) {
        PasswordResetToken resetToken = passwordResetTokenRepo.findByToken(token)
                .orElseThrow(() -> new InvalidPasswordResetTokenException("Invalid password reset token"));

        if (resetToken.isUsed()) {
            throw new InvalidPasswordResetTokenException("Password reset token has already been used");
        }

        if (resetToken.isExpired()) {
            throw new InvalidPasswordResetTokenException("Password reset token has expired");
        }

        User user = resetToken.getUser();
        user.setPassword(encoder.encode(newPassword));
        userRepo.save(user);

        resetToken.setUsed(true);
        passwordResetTokenRepo.save(resetToken);

        log.info("Password successfully reset for user: {}", user.getUserId());
    }
}
