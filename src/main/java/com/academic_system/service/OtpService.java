package com.academic_system.service;

import com.academic_system.exception.BusinessRuleException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class OtpService {

    private final EmailService emailService;
    private final StringRedisTemplate redisTemplate;
    private final SecureRandom random = new SecureRandom();

    private static final String OTP_KEY_PREFIX = "otp:";
    private static final String ATTEMPTS_KEY_PREFIX = "otp:attempts:";
    private static final String LOCKOUT_KEY_PREFIX = "otp:lockout:";
    private static final String VERIFIED_KEY_PREFIX = "otp:verified:";
    private static final String REQUEST_COUNT_KEY_PREFIX = "otp:request:";

    @Value("${otp.expiration-minutes:5}")
    private int expirationMinutes;

    @Value("${otp.length:6}")
    private int otpLength;

    @Value("${otp.max-attempts:6}")
    private int maxAttempts;

    @Value("${otp.lockout-minutes:15}")
    private int lockoutMinutes;

    public String generateOtp(String purpose, String identifier) {
        if (isLockedOut(purpose, identifier)) {
            throw new BusinessRuleException("Cuenta bloqueada. Intenta más tarde.", "User", "account");
        }

        String otp = generateRandomOtp();
        String key = buildKey(purpose, identifier);

        redisTemplate.opsForValue().set(key, otp, expirationMinutes, TimeUnit.MINUTES);
        resetAttempts(purpose, identifier);

        log.debug("OTP generado para {}: {}", purpose, identifier);
        return otp;
    }

    public void sendOtpByEmail(String email, String purpose) {
        if (isLockedOut(purpose, email)) {
            throw new BusinessRuleException("Cuenta bloqueada. Intenta más tarde.", "User", "account");
        }

        String otp = generateOtp(purpose, email);
        String subject;
        String message;

        switch (purpose) {
            case "LOGIN_2FA":
                subject = "Código de verificación - Inicio de sesión";
                message = String.format(
                    "Su código de verificación para iniciar sesión es: %s\n\n" +
                    "Este código expira en %d minutos.\n\n" +
                    "Si no solicitó este código, ignore este mensaje.",
                    otp, expirationMinutes
                );
                break;
            case "PASSWORD_RECOVERY":
                subject = "Código de recuperación de contraseña";
                message = String.format(
                    "Su código de recuperación de contraseña es: %s\n\n" +
                    "Este código expira en %d minutos.\n\n" +
                    "Si no solicitó este código, ignore este mensaje.",
                    otp, expirationMinutes
                );
                break;
            case "EMAIL_VERIFICATION":
                subject = "Verificación de correo electrónico";
                message = String.format(
                    "Su código de verificación es: %s\n\n" +
                    "Este código expira en %d minutos.",
                    otp, expirationMinutes
                );
                break;
            default:
                subject = "Código de verificación";
                message = String.format("Su código de verificación es: %s\n\nExpira en %d minutos.", otp, expirationMinutes);
        }

        emailService.sendEmail(email, subject, message);
    }

    public OtpVerifyResult verifyOtp(String purpose, String identifier, String otp) {
        if (isLockedOut(purpose, identifier)) {
            long remainingMinutes = getLockoutRemainingTime(purpose, identifier);
            return OtpVerifyResult.locked(remainingMinutes);
        }

        String key = buildKey(purpose, identifier);
        String storedOtp = redisTemplate.opsForValue().get(key);

        if (storedOtp == null) {
            log.warn("OTP no encontrado para {}: {}", purpose, identifier);
            return OtpVerifyResult.invalid("Código expirado o no existe");
        }

        if (storedOtp.equals(otp)) {
            redisTemplate.delete(key);
            resetAttempts(purpose, identifier);
            log.info("OTP verificado correctamente para {}: {}", purpose, identifier);
            return OtpVerifyResult.success();
        }

        int attempts = incrementAttempts(purpose, identifier);
        redisTemplate.delete(key);

        if (attempts >= maxAttempts) {
            lockOut(purpose, identifier);
            log.warn("OTP bloqueado por intentos fallidos para {}: {}", purpose, identifier);
            return OtpVerifyResult.locked(lockoutMinutes);
        }

        int remainingAttempts = maxAttempts - attempts;
        log.warn("OTP inválido para {}: {}. Intentos restantes: {}", purpose, identifier, remainingAttempts);
        return OtpVerifyResult.invalid("Código inválido. Intentos restantes: " + remainingAttempts);
    }

    public boolean isLockedOut(String purpose, String identifier) {
        String key = LOCKOUT_KEY_PREFIX + purpose + ":" + identifier;
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }

    public long getLockoutRemainingTime(String purpose, String identifier) {
        String key = LOCKOUT_KEY_PREFIX + purpose + ":" + identifier;
        Long ttl = redisTemplate.getExpire(key, TimeUnit.MINUTES);
        return ttl != null && ttl > 0 ? ttl : 0;
    }

    public void invalidateOtp(String purpose, String identifier) {
        String key = buildKey(purpose, identifier);
        redisTemplate.delete(key);
    }

    public boolean hasPendingOtp(String purpose, String identifier) {
        String key = buildKey(purpose, identifier);
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }

    public void markOtpVerified(String purpose, String identifier) {
        String key = VERIFIED_KEY_PREFIX + purpose + ":" + identifier;
        redisTemplate.opsForValue().set(key, "1", 30, TimeUnit.MINUTES);
    }

    public boolean isOtpVerified(String purpose, String identifier) {
        String key = VERIFIED_KEY_PREFIX + purpose + ":" + identifier;
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }

    public void clearOtpVerified(String purpose, String identifier) {
        String key = VERIFIED_KEY_PREFIX + purpose + ":" + identifier;
        redisTemplate.delete(key);
    }

    public boolean canRequestOtp(String purpose, String identifier) {
        String key = REQUEST_COUNT_KEY_PREFIX + purpose + ":" + identifier;
        Long count = redisTemplate.opsForValue().increment(key);
        if (count != null && count == 1) {
            redisTemplate.expire(key, Duration.ofHours(24));
        }
        return count != null && count <= 10;
    }

    public void resetRequestCount(String purpose, String identifier) {
        String key = REQUEST_COUNT_KEY_PREFIX + purpose + ":" + identifier;
        redisTemplate.delete(key);
    }

    private String generateRandomOtp() {
        StringBuilder otp = new StringBuilder();
        for (int i = 0; i < otpLength; i++) {
            otp.append(random.nextInt(10));
        }
        return otp.toString();
    }

    private String buildKey(String purpose, String identifier) {
        return OTP_KEY_PREFIX + purpose + ":" + identifier;
    }

    private int incrementAttempts(String purpose, String identifier) {
        String key = ATTEMPTS_KEY_PREFIX + purpose + ":" + identifier;
        Long attempts = redisTemplate.opsForValue().increment(key);
        if (attempts == 1) {
            redisTemplate.expire(key, Duration.ofMinutes(lockoutMinutes));
        }
        return attempts != null ? attempts.intValue() : 1;
    }

    private void resetAttempts(String purpose, String identifier) {
        String key = ATTEMPTS_KEY_PREFIX + purpose + ":" + identifier;
        redisTemplate.delete(key);
    }

    private void lockOut(String purpose, String identifier) {
        String key = LOCKOUT_KEY_PREFIX + purpose + ":" + identifier;
        redisTemplate.opsForValue().set(key, "1", lockoutMinutes, TimeUnit.MINUTES);
    }

    public static class OtpVerifyResult {
        private final boolean success;
        private final boolean locked;
        private final String errorMessage;
        private final Long remainingMinutes;

        private OtpVerifyResult(boolean success, boolean locked, String errorMessage, Long remainingMinutes) {
            this.success = success;
            this.locked = locked;
            this.errorMessage = errorMessage;
            this.remainingMinutes = remainingMinutes;
        }

        public static OtpVerifyResult success() {
            return new OtpVerifyResult(true, false, null, null);
        }

        public static OtpVerifyResult invalid(String message) {
            return new OtpVerifyResult(false, false, message, null);
        }

        public static OtpVerifyResult locked(long remainingMinutes) {
            return new OtpVerifyResult(false, true, "Cuenta bloqueada por demasiados intentos fallidos", remainingMinutes);
        }

        public boolean isSuccess() { return success; }
        public boolean isLocked() { return locked; }
        public String getErrorMessage() { return errorMessage; }
        public Long getRemainingMinutes() { return remainingMinutes; }
    }
}
