package com.academic_system.service;

import com.academic_system.entity.postgres.User;
import com.academic_system.repository.postgres.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserSecurityService {

    private final UserRepository userRepository;

    @Value("${security.max-login-attempts:10}")
    private int maxLoginAttempts;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void recordFailedLoginAttempt(String username) {
        userRepository.findByUsername(username).ifPresent(user -> {
            user.incrementFailedAttempts();
            log.info("Intentos fallidos para {}: {}", username, user.getFailedAttempts());
            
            if (user.getFailedAttempts() >= maxLoginAttempts) {
                user.setIsLocked(true);
                log.warn("Usuario {} bloqueado tras {} intentos fallidos", username, user.getFailedAttempts());
            }
            
            userRepository.save(user);
            userRepository.flush();
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
        
        user.setIsLocked(false);
        user.setFailedAttempts(0);
        userRepository.save(user);
        userRepository.flush();
        
        log.info("Usuario {} desbloqueado", user.getUsername());
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void lockUser(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
        
        user.setIsLocked(true);
        user.setFailedAttempts(maxLoginAttempts);
        userRepository.save(user);
        userRepository.flush();
        
        log.warn("Usuario {} bloqueado manualmente", user.getUsername());
    }
}