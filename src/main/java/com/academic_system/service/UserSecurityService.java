package com.academic_system.service;

import com.academic_system.entity.postgres.User;
import com.academic_system.repository.postgres.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserSecurityService {

    private final UserRepository userRepository;
    private final RedisTemplate<String, String> redisTemplate;

    @Value("${security.max-login-attempts:10}")
    private int maxLoginAttempts;

    @Value("${security.lock-duration-minutes:30}")
    private int lockDurationMinutes;

    private static final String REDIS_LOCK_PREFIX = "user:locked:";

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void recordFailedLoginAttempt(String username) {
        userRepository.findByUsername(username).ifPresent(user -> {
            user.incrementFailedAttempts();
            log.info("Intentos fallidos para {}: {}", username, user.getFailedAttempts());
            
            if (user.getFailedAttempts() >= maxLoginAttempts) {
                user.setIsLocked(true);
                userRepository.save(user);
                userRepository.flush();
                
                String redisKey = REDIS_LOCK_PREFIX + user.getId();
                redisTemplate.opsForValue().set(redisKey, "locked", Duration.ofMinutes(lockDurationMinutes));
                
                log.warn("Usuario {} bloqueado tras {} intentos fallidos (TTL: {} min)", 
                        username, user.getFailedAttempts(), lockDurationMinutes);
            } else {
                userRepository.save(user);
                userRepository.flush();
            }
        });
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void resetFailedAttempts(String username) {
        userRepository.findByUsername(username).ifPresent(user -> {
            if (user.getFailedAttempts() == null || user.getFailedAttempts() > 0) {
                user.resetFailedAttempts();
                log.info("Contador de intentos reseteado para: {}", username);
                userRepository.save(user);
                userRepository.flush();
            }
        });
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void unlockUser(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
        
        String redisKey = REDIS_LOCK_PREFIX + userId;
        redisTemplate.delete(redisKey);
        
        user.setIsLocked(false);
        user.setFailedAttempts(0);
        userRepository.save(user);
        userRepository.flush();
        
        log.info("Usuario {} desbloqueado manualmente", user.getUsername());
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void lockUser(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
        
        String redisKey = REDIS_LOCK_PREFIX + userId;
        redisTemplate.opsForValue().set(redisKey, "locked", Duration.ofMinutes(lockDurationMinutes));
        
        user.setIsLocked(true);
        user.setFailedAttempts(maxLoginAttempts);
        userRepository.save(user);
        userRepository.flush();
        
        log.warn("Usuario {} bloqueado manualmente (TTL: {} min)", user.getUsername(), lockDurationMinutes);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public boolean checkAndAutoUnlock(String username) {
        return userRepository.findByUsername(username).map(user -> {
            if (Boolean.TRUE.equals(user.getIsLocked())) {
                String redisKey = REDIS_LOCK_PREFIX + user.getId();
                Boolean hasKey = redisTemplate.hasKey(redisKey);
                
                if (Boolean.FALSE.equals(hasKey)) {
                    user.setIsLocked(false);
                    user.setFailedAttempts(0);
                    userRepository.save(user);
                    userRepository.flush();
                    log.info("Usuario {} desbloqueado automaticamente (TTL expirado)", username);
                    return true;
                }
                return false;
            }
            return true;
        }).orElse(true);
    }
}