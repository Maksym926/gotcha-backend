package com.gotcha.gotcha_api.securityConfig;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gotcha.gotcha_api.exception.ErrorResponse;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    @Override
    public void commence(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException authException
    ) throws IOException {

        response.setContentType("application/json");
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

        Map<String, String> errors = new HashMap<>();
        errors.put("message", "Invalid email or password");

        ErrorResponse errorResponse =
                new ErrorResponse(LocalDateTime.now(), 401, errors);

        objectMapper.writeValue(response.getOutputStream(), errorResponse);
    }
}
