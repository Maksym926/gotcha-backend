package com.gotcha.gotcha_api.securityConfig;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class RateLimitingFilter extends OncePerRequestFilter {

    // Sensitive endpoints: 10 requests per minute per IP (brute-force protection)
    private final ConcurrentHashMap<String, Bucket> authBuckets = new ConcurrentHashMap<>();

    // General endpoints: 100 requests per minute per IP
    private final ConcurrentHashMap<String, Bucket> generalBuckets = new ConcurrentHashMap<>();

    private Bucket createAuthBucket() {
        return Bucket.builder()
                .addLimit(Bandwidth.builder()
                        .capacity(10)
                        .refillIntervally(10, Duration.ofMinutes(1))
                        .build())
                .build();
    }

    private Bucket createGeneralBucket() {
        return Bucket.builder()
                .addLimit(Bandwidth.builder()
                        .capacity(100)
                        .refillIntervally(100, Duration.ofMinutes(1))
                        .build())
                .build();
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String ip = getClientIp(request);
        String uri = request.getRequestURI();

        boolean isSensitiveEndpoint = uri.equals("/api/login")
                || uri.equals("/api/register")
                || uri.equals("/api/forgot-password");

        Bucket bucket = isSensitiveEndpoint
                ? authBuckets.computeIfAbsent(ip, k -> createAuthBucket())
                : generalBuckets.computeIfAbsent(ip, k -> createGeneralBucket());

        if (bucket.tryConsume(1)) {
            filterChain.doFilter(request, response);
        } else {
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.getWriter().write("{\"status\":429,\"message\":\"Too many requests. Please slow down.\"}");
        }
    }

    private String getClientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            // X-Forwarded-For can contain multiple IPs: "client, proxy1, proxy2"
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
