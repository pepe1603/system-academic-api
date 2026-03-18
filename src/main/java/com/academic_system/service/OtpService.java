package com.academic_system.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
@Slf4j
public class OtpService {

    private final EmailService emailService;
    private final SecureRandom random = new SecureRandom();

    @Value("${otp.expiration-minutes:5}")
    private int expirationMinutes;

    @Value("${otp.length:6}")
    private int otpLength;

    private final ConcurrentHashMap<String, OtpEntry> otpStore = new ConcurrentHashMap<>();

    public String generateOtp(String purpose, String identifier) {
        String otp = generateRandomOtp();
        String key = buildKey(purpose, identifier);

        otpStore.put(key, new OtpEntry(otp, Instant.now().plusSeconds(expirationMinutes * 60L)));

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
                    "Este código expire en %d minutos.\n\n" +
                    "Si no solicitó este código, ignore este mensaje.",
                    otp, expirationMinutes
                );
                break;
            case "PASSWORD_RECOVERY":
                subject = "Código de recuperación de contraseña";
                message = String.format(
                    "Su código de recuperación de contraseña es: %s\n\n" +
                    "Este código expire en %d minutos.\n\n" +
                    "Si no solicitó este código, ignore este mensaje.",
                    otp, expirationMinutes
                );
                break;
            case "EMAIL_VERIFICATION":
                subject = "Verificación de correo electrónico";
                message = String.format(
                    "Su código de verificación es: %s\n\n" +
                    "Este código expire en %d minutos.",
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
        OtpEntry entry = otpStore.get(key);

        if (entry == null) {
            log.warn("OTP no encontrado para {}: {}", purpose, identifier);
            return false;
        }

        if (Instant.now().isAfter(entry.expiresAt)) {
            otpStore.remove(key);
            log.warn("OTP expirado para {}: {}", purpose, identifier);
            return false;
        }

        if (entry.otp.equals(otp)) {
            otpStore.remove(key);
            log.info("OTP verificado correctamente para {}: {}", purpose, identifier);
            return true;
        }

        log.warn("OTP inválido para {}: {}", purpose, identifier);
        return false;
    }

    public void invalidateOtp(String purpose, String identifier) {
        String key = buildKey(purpose, identifier);
        otpStore.remove(key);
    }

    public boolean hasPendingOtp(String purpose, String identifier) {
        String key = buildKey(purpose, identifier);
        OtpEntry entry = otpStore.get(key);
        
        if (entry == null) {
            return false;
        }

        if (Instant.now().isAfter(entry.expiresAt)) {
            otpStore.remove(key);
            return false;
        }

        return true;
    }

    private String generateRandomOtp() {
        StringBuilder otp = new StringBuilder();
        for (int i = 0; i < otpLength; i++) {
            otp.append(random.nextInt(10));
        }
        return otp.toString();
    }

    private String buildKey(String purpose, String identifier) {
        return purpose + ":" + identifier;
    }

    private static class OtpEntry {
        String otp;
        Instant expiresAt;

        OtpEntry(String otp, Instant expiresAt) {
            this.otp = otp;
            this.expiresAt = expiresAt;
        }
    }
}
