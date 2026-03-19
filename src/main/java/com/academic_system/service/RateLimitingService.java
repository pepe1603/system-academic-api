package com.academic_system.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class RateLimitingService {

    private final StringRedisTemplate redisTemplate;

    private static final String RATE_LIMIT_PREFIX = "rate_limit:";

    public boolean isAllowed(String key, int maxAttempts, int windowMinutes) {
        String redisKey = RATE_LIMIT_PREFIX + key;
        Long currentAttempts = redisTemplate.opsForValue().increment(redisKey);

        if (currentAttempts == null) {
            return false;
        }

        if (currentAttempts == 1) {
            redisTemplate.expire(redisKey, windowMinutes, TimeUnit.MINUTES);
        }

        boolean allowed = currentAttempts <= maxAttempts;
        
        if (!allowed) {
            log.warn("Rate limit exceeded for key: {}", key);
        }

        return allowed;
    }

    public int getRemainingAttempts(String key, int maxAttempts) {
        String redisKey = RATE_LIMIT_PREFIX + key;
        String value = redisTemplate.opsForValue().get(redisKey);
        
        if (value == null) {
            return maxAttempts;
        }

        return Math.max(0, maxAttempts - Integer.parseInt(value));
    }

    public void resetLimit(String key) {
        String redisKey = RATE_LIMIT_PREFIX + key;
        redisTemplate.delete(redisKey);
    }

    public boolean isLoginAllowed(String ip) {
        return isAllowed("login:" + ip, 5, 15);
    }

    public boolean isRecoveryAllowed(String email) {
        return isAllowed("recovery:" + email, 3, 60);
    }

    public boolean isOtpAllowed(String email) {
        return isAllowed("otp:" + email, 3, 10);
    }
}
