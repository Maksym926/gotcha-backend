package com.gotcha.gotcha_api.service;

import com.gotcha.gotcha_api.repo.EmailVerificationTokenRepo;
import com.gotcha.gotcha_api.repo.PasswordResetTokenRepo;
import com.gotcha.gotcha_api.repo.RefreshTokenRepo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class TokenCleanupService {

    private static final Logger log = LoggerFactory.getLogger(TokenCleanupService.class);

    private final RefreshTokenRepo refreshTokenRepo;
    private final EmailVerificationTokenRepo emailVerificationTokenRepo;
    private final PasswordResetTokenRepo passwordResetTokenRepo;

    public TokenCleanupService(RefreshTokenRepo refreshTokenRepo,
                               EmailVerificationTokenRepo emailVerificationTokenRepo,
                               PasswordResetTokenRepo passwordResetTokenRepo) {
        this.refreshTokenRepo = refreshTokenRepo;
        this.emailVerificationTokenRepo = emailVerificationTokenRepo;
        this.passwordResetTokenRepo = passwordResetTokenRepo;
    }

    @Scheduled(cron = "0 0 3 * * *")
    @Transactional
    public void cleanUpExpiredTokens() {
        LocalDateTime now = LocalDateTime.now();

        int refreshDeleted = refreshTokenRepo.deleteExpiredTokens(now);
        int emailDeleted = emailVerificationTokenRepo.deleteExpiredTokens(now);
        int passwordDeleted = passwordResetTokenRepo.deleteExpiredTokens(now);

        log.info("Token cleanup completed: {} refresh, {} email verification, {} password reset tokens deleted",
                refreshDeleted, emailDeleted, passwordDeleted);
    }
}
