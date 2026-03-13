package com.example.expensestracker.config;

import com.example.expensestracker.service.RateLimitingService;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.ConsumptionProbe;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class RateLimitFilter extends OncePerRequestFilter {

    private final RateLimitingService rateLimitingService;
    private List<String> sensitiveEndpoints;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain)
            throws ServletException, IOException {
        if (sensitiveEndpoints == null) {
            sensitiveEndpoints = new ArrayList<>();
            sensitiveEndpoints.add( "/api/users/login");
            sensitiveEndpoints.add( "/api/users/register");
            sensitiveEndpoints.add( "/api/users/send-otp");
            sensitiveEndpoints.add( "/api/users/verify-otp");
            sensitiveEndpoints.add("/api/users/reset-password");
        }
        String requestURI = request.getRequestURI();

        boolean isSensitive = sensitiveEndpoints.stream().anyMatch(requestURI::startsWith);

        if (isSensitive) {
            String clientIp = getClientIp(request);
            Bucket bucket = rateLimitingService.resolveBucket(clientIp);
            ConsumptionProbe probe = bucket.tryConsumeAndReturnRemaining(1);

            if (!probe.isConsumed()) {
                log.warn("Rate limit exceeded for IP: {}", clientIp);
                response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
                response.getWriter().write("Too many requests - Please try again later");
                return;
            }

            // Add rate limit headers
            response.addHeader("X-Rate-Limit-Remaining", String.valueOf(probe.getRemainingTokens()));
        }

        filterChain.doFilter(request, response);
    }

    private String getClientIp(HttpServletRequest request) {
        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader == null) {
            return request.getRemoteAddr();
        }
        return xfHeader.split(",")[0];
    }
}
