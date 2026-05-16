package com.backend.CodeSheriff.Security;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class RateLimitingFilter extends OncePerRequestFilter {

    private final Map<String, Bucket> cache = new ConcurrentHashMap<>();

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String key = getClientKey(request);
        Bucket bucket = cache.computeIfAbsent(key, k -> createBucket());

        if (bucket.tryConsume(1)) {
            filterChain.doFilter(request, response);
        } else {
            log.warn("Rate limit exceeded for client: {}", key);
            response.setStatus(429); // Too Many Requests
            response.setContentType("application/json");
            response.getWriter().write(
                "{\"error\":\"Rate limit exceeded\",\"message\":\"Too many requests. Please try again later.\"}"
            );
        }
    }

    private Bucket createBucket() {
        // Allow 20 requests per minute per user/IP
        Bandwidth limit = Bandwidth.classic(20, Refill.intervally(20, Duration.ofMinutes(1)));
        return Bucket.builder()
            .addLimit(limit)
            .build();
    }

    private String getClientKey(HttpServletRequest request) {
        // Try to get authenticated user ID first
        try {
            Object principal = SecurityContextHolder.getContext()
                .getAuthentication()
                .getPrincipal();
            if (principal != null && !principal.equals("anonymousUser")) {
                return "user:" + principal.toString();
            }
        } catch (Exception e) {
            // Not authenticated, fall back to IP
        }

        // Fall back to IP address
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty()) {
            ip = request.getRemoteAddr();
        }
        return "ip:" + ip;
    }
}

// Made with Bob
