package com.gotcha.gotcha_api.service;

import com.gotcha.gotcha_api.exception.custom.InvalidRefreshTokenException;
import com.gotcha.gotcha_api.model.RefreshToken;
import com.gotcha.gotcha_api.model.User;
import com.gotcha.gotcha_api.model.dto.AuthResponse;
import com.gotcha.gotcha_api.repo.RefreshTokenRepo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@Slf4j
public class RefreshTokenService {

    private static final int REFRESH_TOKEN_EXPIRY_DAYS = 7;

    @Autowired
    private RefreshTokenRepo refreshTokenRepo;

    @Autowired
    private JWTService jwtService;

    public RefreshToken createRefreshToken(User user) {
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setToken(UUID.randomUUID().toString());
        refreshToken.setFamily(UUID.randomUUID().toString());
        refreshToken.setUser(user);
        refreshToken.setRevoked(false);
        refreshToken.setExpiryDate(LocalDateTime.now().plusDays(REFRESH_TOKEN_EXPIRY_DAYS));
        refreshToken.setCreatedAt(LocalDateTime.now());

        return refreshTokenRepo.save(refreshToken);
    }

    @Transactional
    public AuthResponse rotateRefreshToken(String tokenValue) {
        RefreshToken existingToken = refreshTokenRepo.findByToken(tokenValue)
                .orElseThrow(() -> new InvalidRefreshTokenException("Invalid refresh token"));

        if (existingToken.isExpired()) {
            throw new InvalidRefreshTokenException("Refresh token has expired");
        }

        // Token was already rotated
        if (existingToken.getRotatedAt() != null) {
            // Still within grace period — return same family new tokens without re-rotating
            if (existingToken.isWithinGracePeriod()) {
                log.debug("Refresh token used within grace period, allowing");
                String accessToken = jwtService.generateToken(existingToken.getUser().getEmail());
                // Create a new refresh token in the same family
                RefreshToken newToken = createRefreshTokenInFamily(existingToken.getUser(), existingToken.getFamily());
                return new AuthResponse(accessToken, newToken.getToken());
            }

            // Past grace period — this is token reuse, likely theft
            log.warn("Refresh token reuse detected for family: {}. Revoking entire family.", existingToken.getFamily());
            refreshTokenRepo.revokeFamily(existingToken.getFamily());
            throw new InvalidRefreshTokenException("Refresh token has already been used. All sessions revoked for security.");
        }

        if (existingToken.isRevoked()) {
            throw new InvalidRefreshTokenException("Refresh token has been revoked");
        }

        // Mark old token as rotated with timestamp for grace period
        existingToken.setRotatedAt(LocalDateTime.now());
        refreshTokenRepo.save(existingToken);

        // Issue new refresh token in the same family
        RefreshToken newToken = createRefreshTokenInFamily(existingToken.getUser(), existingToken.getFamily());

        String accessToken = jwtService.generateToken(existingToken.getUser().getEmail());

        return new AuthResponse(accessToken, newToken.getToken());
    }

    private RefreshToken createRefreshTokenInFamily(User user, String family) {
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setToken(UUID.randomUUID().toString());
        refreshToken.setFamily(family);
        refreshToken.setUser(user);
        refreshToken.setRevoked(false);
        refreshToken.setExpiryDate(LocalDateTime.now().plusDays(REFRESH_TOKEN_EXPIRY_DAYS));
        refreshToken.setCreatedAt(LocalDateTime.now());

        return refreshTokenRepo.save(refreshToken);
    }

    @Transactional
    public void revokeTokenFamily(String tokenValue) {
        refreshTokenRepo.findByToken(tokenValue).ifPresent(token ->
                refreshTokenRepo.revokeFamily(token.getFamily())
        );
    }

    @Transactional
    public void revokeAllUserTokens(Long userId) {
        refreshTokenRepo.deleteByUserId(userId);
    }
}
