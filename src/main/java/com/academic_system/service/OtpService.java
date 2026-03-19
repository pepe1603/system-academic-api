package com.academic_system.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class OtpService {

    private final EmailService emailService;
    private final StringRedisTemplate redisTemplate;
    private final SecureRandom random = new SecureRandom();

    private static final String OTP_KEY_PREFIX = "otp:";

    @Value("${otp.expiration-minutes:5}")
    private int expirationMinutes;

    @Value("${otp.length:6}")
    private int otpLength;

    public String generateOtp(String purpose, String identifier) {
        String otp = generateRandomOtp();
        String key = buildKey(purpose, identifier);

        redisTemplate.opsForValue().set(key, otp, expirationMinutes, TimeUnit.MINUTES);

        log.debug("OTP generado para {}: {}", purpose, identifier);
        return otp;
    }

    public void sendOtpByEmail(String email, String purpose) {
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

    public boolean verifyOtp(String purpose, String identifier, String otp) {
        String key = buildKey(purpose, identifier);
        String storedOtp = redisTemplate.opsForValue().get(key);

        if (storedOtp == null) {
            log.warn("OTP no encontrado para {}: {}", purpose, identifier);
            return false;
        }

        if (storedOtp.equals(otp)) {
            redisTemplate.delete(key);
            log.info("OTP verificado correctamente para {}: {}", purpose, identifier);
            return true;
        }

        log.warn("OTP inválido para {}: {}", purpose, identifier);
        return false;
    }

    public void invalidateOtp(String purpose, String identifier) {
        String key = buildKey(purpose, identifier);
        redisTemplate.delete(key);
    }

    public boolean hasPendingOtp(String purpose, String identifier) {
        String key = buildKey(purpose, identifier);
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
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
}
