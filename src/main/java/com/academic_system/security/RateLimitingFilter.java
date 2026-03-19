package com.academic_system.security;

import com.academic_system.service.RateLimitingService;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class RateLimitingFilter implements Filter {

    private final RateLimitingService rateLimitingService;

    private static final int MAX_REQUESTS_PER_MINUTE = 100;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        String path = httpRequest.getRequestURI();

        if (path.startsWith("/api/")) {
            String clientIp = getClientIp(httpRequest);
            String key = "api:" + clientIp;

            if (rateLimitingService.isAllowed(key, MAX_REQUESTS_PER_MINUTE, 1)) {
                int remaining = rateLimitingService.getRemainingAttempts(key, MAX_REQUESTS_PER_MINUTE);
                httpResponse.setHeader("X-Rate-Limit-Remaining", String.valueOf(remaining));
                chain.doFilter(request, response);
            } else {
                httpResponse.setStatus(429);
                httpResponse.setContentType("application/json");
                httpResponse.getWriter().write(
                    "{\"error\": \"Demasiadas solicitudes. Intente más tarde.\", \"code\": \"RATE_LIMIT_EXCEEDED\"}"
                );
            }
        } else {
            chain.doFilter(request, response);
        }
    }

    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
