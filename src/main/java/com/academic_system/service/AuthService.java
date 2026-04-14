package com.academic_system.service;

import com.academic_system.dto.auth.*;

import com.academic_system.entity.postgres.User;
import com.academic_system.entity.postgres.UserSession;
import com.academic_system.exception.PasswordChangeRequiredException;
import com.academic_system.repository.postgres.PasswordRecoveryRepository;
import com.academic_system.repository.postgres.UserRepository;
import com.academic_system.repository.postgres.UserSessionRepository;
import com.academic_system.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Random;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordRecoveryRepository passwordRecoveryRepository;
    private final UserSessionRepository userSessionRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final OtpService otpService;
    private final AuthenticationManager authenticationManager;
    private final EmailService emailService;

    @Value("${security.password.expiry-days:90}")
    private int passwordExpiryDays;

    @Value("${security.max-login-attempts:5}")
    private int maxLoginAttempts;

    @Transactional
    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new BadCredentialsException("Credenciales inválidas"));

        // Verificar si la cuenta está bloqueada
        if (user.getIsLocked()) {
            throw new LockedException("Cuenta bloqueada por intentos fallidos. Contacte al administrador.");
        }

        // Verificar si debe cambiar contraseña
        if (Boolean.TRUE.equals(user.getMustChangePassword())) {
            throw new PasswordChangeRequiredException("Debe cambiar su contraseña antes de continuar");
        }

        // Verificar si la contraseña está expirada
        if (user.isPasswordExpired(passwordExpiryDays)) {
            user.setMustChangePassword(true);
            userRepository.save(user);
            throw new PasswordChangeRequiredException("Su contraseña ha expirado. Debe cambiarla.");
        }

        // Autenticar
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword()
                )
        );

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

        // Si tiene 2FA habilitado, enviar código OTP por email
        if (Boolean.TRUE.equals(user.getTwoFactorEnabled())) {
            otpService.sendOtpByEmail(user.getEmail(), "LOGIN_2FA");
            
            String tempToken = jwtService.generateToken(
                    java.util.Map.of("userId", user.getId().toString(), "pending2fa", "true"),
                    userDetails,
                    300000
            );

            return LoginResponse.builder()
                    .userId(user.getId())
                    .username(user.getUsername())
                    .email(user.getEmail())
                    .requiresTwoFactor(true)
                    .tempToken(tempToken)
                    .message("Se ha enviado un código de verificación a su correo")
                    .build();
        }

        // Login exitoso sin 2FA
        return completeLogin(user);
    }

    @Transactional
    public LoginResponse verifyOtp(String tempToken, String otpCode) {
        String username = jwtService.extractUsername(tempToken);
        
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new BadCredentialsException("Usuario no encontrado"));

        if (!otpService.verifyOtp("LOGIN_2FA", user.getEmail(), otpCode).isSuccess()) {
            user.incrementFailedAttempts();
            userRepository.save(user);
            throw new BadCredentialsException("Código de verificación inválido o expirado");
        }

        return completeLogin(user);
    }



    private LoginResponse completeLogin(User user) {
        user.resetFailedAttempts();
        user.setLastLogin(LocalDateTime.now());
        userRepository.save(user);

        CustomUserDetails userDetails = new CustomUserDetails(user);

        String accessToken = jwtService.generateAccessToken(userDetails);
        String refreshToken = jwtService.generateRefreshToken(userDetails);

        UserSession session = UserSession.builder()
                .user(user)
                .jwtToken(accessToken)
                .refreshToken(refreshToken)
                .expiresAt(LocalDateTime.now().plusSeconds(jwtService.getAccessTokenExpiration() / 1000))
                .build();
        userSessionRepository.save(session);

        return LoginResponse.builder()
                .userId(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .roles(user.getRoles().stream()
                        .map(r -> r.getName())
                        .collect(Collectors.toSet()))
                .permissions(user.getRoles().stream()
                        .flatMap(r -> r.getPermissions().stream())
                        .map(p -> p.getCode())
                        .collect(Collectors.toSet()))
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .expiresIn(jwtService.getAccessTokenExpiration())
                .build();
    }

    @Transactional
    public ApiResponse<String> refreshToken(RefreshTokenRequest request) {
        String refreshToken = request.getRefreshToken();

        String username;
        try {
            username = jwtService.extractUsername(refreshToken);
        } catch (Exception e) {
            log.error("Refresh token inválido: {}", e.getMessage());
            throw new BadCredentialsException("Refresh token inválido o expirado");
        }

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new BadCredentialsException("Usuario no encontrado"));

        if (!jwtService.isRefreshTokenValid(refreshToken, user)) {
            throw new BadCredentialsException("Refresh token inválido o expirado");
        }

        UserSession session = userSessionRepository.findByJwtTokenAndIsActiveTrue(refreshToken)
                .orElseGet(() -> userSessionRepository.findByRefreshTokenAndIsActiveTrue(refreshToken)
                        .orElse(null));

        if (session == null || !session.getIsActive()) {
            throw new BadCredentialsException("Sesión invalidada. Por favor, inicie sesión nuevamente.");
        }

        CustomUserDetails userDetails = new CustomUserDetails(user);
        String newAccessToken = jwtService.generateAccessToken(userDetails);

        return ApiResponse.success("Token refrescado", newAccessToken);
    }

    @Transactional
    public ApiResponse<Void> logout(String authorizationHeader) {
        String token = extractTokenFromHeader(authorizationHeader);
        
        String username;
        try {
            username = jwtService.extractUsername(token);
        } catch (Exception e) {
            log.error("Token inválido en logout: {}", e.getMessage());
            return ApiResponse.error("Token inválido o corrupto");
        }
        
        User user = userRepository.findByUsername(username)
                .orElse(null);
        
        if (user != null) {
            userSessionRepository.invalidateAllSessionsForUser(user.getId());
        }
        
        SecurityContextHolder.clearContext();
        return ApiResponse.success("Sesión cerrada exitosamente", null);
    }
    
    private String extractTokenFromHeader(String authorizationHeader) {
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            throw new BadCredentialsException("Token no proporcionado");
        }
        return authorizationHeader.substring(7);
    }

    // === RECUPERACIÓN DE CONTRASEÑA ===

    public ApiResponse<Void> requestPasswordRecovery(PasswordRecoveryRequest request) {
        String email = request.getEmail();
        
        if (!otpService.canRequestOtp("PASSWORD_RECOVERY", email)) {
            return ApiResponse.error("Ha excedido el límite de solicitudes. Intente de nuevo en 24 horas");
        }
        
        userRepository.findByEmail(email)
                .ifPresent(user -> {
                    otpService.sendOtpByEmail(user.getEmail(), "PASSWORD_RECOVERY");
                });

        return ApiResponse.success("Si el email existe, se enviará un código de recuperación", null);
    }

    public ApiResponse<Void> resetPassword(ResetPasswordRequest request) {
        String email = request.getEmail();
        String code = request.getToken();

        if (!otpService.isOtpVerified("PASSWORD_RECOVERY", email)) {
            return ApiResponse.error("Debe verificar el código OTP antes de restablecer la contraseña");
        }

        OtpService.OtpVerifyResult result = otpService.verifyOtp("PASSWORD_RECOVERY", email, code);
        
        if (!result.isSuccess()) {
            if (result.isLocked()) {
                return ApiResponse.error(result.getErrorMessage() + ". Intenta en " + result.getRemainingMinutes() + " minutos");
            }
            return ApiResponse.error(result.getErrorMessage());
        }

        User user = userRepository.findByEmail(email)
                .orElse(null);

        if (user == null) {
            return ApiResponse.error("Usuario no encontrado");
        }

        if (passwordEncoder.matches(request.getNewPassword(), user.getPasswordHash())) {
            return ApiResponse.error("La nueva contraseña debe ser diferente a la anterior");
        }

        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        user.setPasswordChangedAt(LocalDateTime.now());
        user.setMustChangePassword(false);
        userRepository.save(user);

        otpService.clearOtpVerified("PASSWORD_RECOVERY", email);

        return ApiResponse.success("Contraseña restablecida exitosamente", null);
    }

    // === 2FA con OTP propio ===

    @Transactional
    public ApiResponse<Void> requestTwoFactorSetup(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

        otpService.sendOtpByEmail(user.getEmail(), "LOGIN_2FA");
        
        return ApiResponse.success("Se ha enviado un código de verificación a su correo electrónico", null);
    }

    @Transactional
    public ApiResponse<Void> enableTwoFactor(UUID userId, String code) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

        if (!otpService.verifyOtp("LOGIN_2FA", user.getEmail(), code).isSuccess()) {
            return ApiResponse.error("Código de verificación inválido o expirado");
        }

        user.setTwoFactorEnabled(true);
        userRepository.save(user);

        return ApiResponse.success("Autenticación de dos factores habilitada correctamente", null);
    }

    @Transactional
    public ApiResponse<Void> disableTwoFactor(UUID userId, String password) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

        if (!passwordEncoder.matches(password, user.getPasswordHash())) {
            return ApiResponse.error("Contraseña incorrecta");
        }

        user.setTwoFactorEnabled(false);
        userRepository.save(user);

        return ApiResponse.success("Autenticación de dos factores deshabilitada", null);
    }

    // === CAMBIO DE CONTRASEÑA FORZADO ===

    @Transactional
    public ApiResponse<Void> changePassword(ChangePasswordRequest request) {
        CustomUserDetails userDetails = (CustomUserDetails) 
                SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        
        User user = userRepository.findById(userDetails.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPasswordHash())) {
            return ApiResponse.error("Contraseña actual incorrecta");
        }

        if (passwordEncoder.matches(request.getNewPassword(), user.getPasswordHash())) {
            return ApiResponse.error("La nueva contraseña debe ser diferente a la anterior");
        }

        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        user.setPasswordChangedAt(LocalDateTime.now());
        user.setMustChangePassword(false);
        userRepository.save(user);

        return ApiResponse.success("Contraseña cambiada exitosamente", null);
    }
}
