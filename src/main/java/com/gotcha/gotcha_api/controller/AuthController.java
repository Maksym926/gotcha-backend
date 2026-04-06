package com.gotcha.gotcha_api.controller;

import com.gotcha.gotcha_api.enums.AccountStatus;
import com.gotcha.gotcha_api.exception.custom.InvalidPasswordResetTokenException;
import com.gotcha.gotcha_api.exception.custom.InvalidRefreshTokenException;
import com.gotcha.gotcha_api.model.EmailVerificationToken;
import com.gotcha.gotcha_api.model.User;
import com.gotcha.gotcha_api.model.RefreshToken;
import com.gotcha.gotcha_api.model.dto.*;
import com.gotcha.gotcha_api.repo.EmailVerificationTokenRepo;
import com.gotcha.gotcha_api.repo.UserRepo;
import com.gotcha.gotcha_api.service.*;
import com.gotcha.gotcha_api.util.CookieUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@Slf4j
@Tag(name = "Authentication", description = "Login, logout, token refresh, registration, and password management")
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
    private RefreshTokenService refreshTokenService;

    @Autowired
    private EmailVerificationTokenRepo emailVerificationTokenRepo;

    @Autowired
    private UserRepo userRepo;

    @PostMapping("/register")
    public ResponseEntity<UserResponse> registerUser(@Valid @RequestBody RegisterRequest registerRequest){
        User user = userService.saveUser(registerRequest);
        return ResponseEntity.ok(new UserResponse(
                user.getUserId(),
                user.getUsername(),
                user.getEmail(),
                user.getStatus(),
                user.getGotchaCoins(),
                user.getProfilePictureKey(),
                user.getRole(),
                user.getMood(),
                user.getSubscriptionStatus(),
                user.getGotchaFavDrink()
        ));
    }

    @Operation(
            summary = "Login",
            description = "Authenticates the user and returns an access token in the response body. " +
                    "A refresh token is set as an HTTP-only secure cookie (not visible to JavaScript).",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Login successful",
                            headers = @Header(name = "Set-Cookie",
                                    description = "refreshToken cookie (HttpOnly, Secure, SameSite=None, Path=/api, Max-Age=7 days)",
                                    schema = @Schema(type = "string"))),
                    @ApiResponse(responseCode = "401", description = "Invalid credentials")
            }
    )
    @PostMapping("/login")
    public ResponseEntity<String> loginUser(@Valid @RequestBody LoginRequest loginRequest,
                                                  HttpServletResponse response) {
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(loginRequest.email(), loginRequest.password()));
        User user = userRepo.findByEmail(loginRequest.email())
                .orElseThrow(() -> new RuntimeException("User not found"));
        String accessToken = jwtService.generateToken(user.getEmail(), user.getUserId(), user.getRole().name());
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user);

        response.addHeader(HttpHeaders.SET_COOKIE, CookieUtil.createRefreshTokenCookie(refreshToken.getToken()).toString());

        return ResponseEntity.ok(accessToken);
    }

    @Operation(
            summary = "Refresh access token",
            description = "Rotates the refresh token and returns a new access token. " +
                    "No request body needed — the refresh token is read from the HTTP-only cookie sent automatically by the browser. " +
                    "A new refresh token cookie replaces the old one.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Token refreshed successfully",
                            headers = @Header(name = "Set-Cookie",
                                    description = "New rotated refreshToken cookie",
                                    schema = @Schema(type = "string"))),
                    @ApiResponse(responseCode = "401", description = "Missing, expired, or revoked refresh token")
            }
    )
    @PostMapping("/refresh")
    public ResponseEntity<String> refreshToken(HttpServletRequest request, HttpServletResponse response) {
        String refreshTokenValue = extractRefreshTokenCookie(request);
        if (refreshTokenValue == null) {
            throw new InvalidRefreshTokenException("Refresh token cookie is missing");
        }

        RefreshToken newRefreshToken = refreshTokenService.rotateRefreshToken(refreshTokenValue);
        User user = newRefreshToken.getUser();
        String accessToken = jwtService.generateToken(user.getEmail(), user.getUserId(), user.getRole().name());

        response.addHeader(HttpHeaders.SET_COOKIE, CookieUtil.createRefreshTokenCookie(newRefreshToken.getToken()).toString());

        return ResponseEntity.ok(accessToken);
    }

    @Operation(
            summary = "Logout",
            description = "Blacklists the access token and revokes the refresh token family. " +
                    "The refresh token cookie is cleared. No request body needed.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Logged out successfully",
                            headers = @Header(name = "Set-Cookie",
                                    description = "Clears the refreshToken cookie (Max-Age=0)",
                                    schema = @Schema(type = "string")))
            }
    )
    @PostMapping("/logout")
    public ResponseEntity<String> logout(@RequestHeader("Authorization") String authHeader,
                                         HttpServletRequest request, HttpServletResponse response) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            tokenBlacklistService.blacklist(authHeader.substring(7));
        }

        String refreshTokenValue = extractRefreshTokenCookie(request);
        if (refreshTokenValue != null) {
            refreshTokenService.revokeTokenFamily(refreshTokenValue);
        }

        response.addHeader(HttpHeaders.SET_COOKIE, CookieUtil.deleteRefreshTokenCookie().toString());

        return ResponseEntity.ok("Logged out successfully");
    }

    private String extractRefreshTokenCookie(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("refreshToken".equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
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
