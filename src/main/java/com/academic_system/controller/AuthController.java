package com.academic_system.controller;

import com.academic_system.dto.auth.*;
import com.academic_system.entity.User;
import com.academic_system.service.AuthService;
import com.academic_system.service.OtpService;
import com.academic_system.service.UserRegistrationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final OtpService otpService;
    private final UserRegistrationService userRegistrationService;

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = authService.login(request);
        return ResponseEntity.ok(ApiResponse.success("Login exitoso", response));
    }

    @PostMapping("/verify-2fa")
    public ResponseEntity<ApiResponse<LoginResponse>> verifyTwoFactor(@Valid @RequestBody TwoFactorRequest request) {
        LoginResponse response = authService.verifyOtp(request.getTempToken(), request.getCode());
        return ResponseEntity.ok(ApiResponse.success("Verificación exitosa", response));
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<String>> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        return ResponseEntity.ok(authService.refreshToken(request));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader) {
        
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(401)
                    .body(ApiResponse.error("Token no proporcionado o formato inválido"));
        }
        
        return ResponseEntity.ok(authService.logout(authorizationHeader));
    }

    @PostMapping("/recovery")
    public ResponseEntity<ApiResponse<Void>> requestRecovery(@Valid @RequestBody PasswordRecoveryRequest request) {
        return ResponseEntity.ok(authService.requestPasswordRecovery(request));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse<Void>> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        return ResponseEntity.ok(authService.resetPassword(request));
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<ApiResponse<Void>> verifyOtp(@Valid @RequestBody VerifyOtpRequest request) {
        String purpose = request.getPurpose() != null ? request.getPurpose() : "PASSWORD_RECOVERY";
        OtpService.OtpVerifyResult result = otpService.verifyOtp(purpose, request.getEmail(), request.getCode());

        if (result.isSuccess()) {
            return ResponseEntity.ok(ApiResponse.success("Código verificado correctamente", null));
        } else if (result.isLocked()) {
            return ResponseEntity.status(429)
                    .body(ApiResponse.error(result.getErrorMessage() + ". Intenta en " + result.getRemainingMinutes() + " minutos"));
        } else {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(result.getErrorMessage()));
        }
    }

    @PostMapping("/change-password")
    public ResponseEntity<ApiResponse<Void>> changePassword(
            @Valid @RequestBody ChangePasswordRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        if (!request.isPasswordMatch()) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Las contraseñas no coinciden"));
        }
        
        return ResponseEntity.ok(authService.changePassword(request));
    }

    // === REGISTRATION ENDPOINTS ===

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<Void>> register(@Valid @RequestBody RegisterRequest request) {
        // Validar tipo de registro
        if (request.getType() == RegisterRequest.RegisterType.GENERAL) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Tipo de registro no válido para auto-registro"));
        }
        
        userRegistrationService.register(request);
        return ResponseEntity.ok(ApiResponse.success("Usuario registrado exitosamente. Puede iniciar sesión.", null));
    }

    // === ADMIN REGISTRATION (requires ADMIN role) ===

    @PostMapping("/admin/register")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> registerByAdmin(
            @RequestParam String username,
            @RequestParam String email,
            @RequestParam String temporaryPassword,
            @RequestParam RegisterRequest.RegisterType type,
            @RequestParam(required = false) String identifier) {
        
        userRegistrationService.createUserByAdmin(username, email, temporaryPassword, type, identifier);
        return ResponseEntity.ok(ApiResponse.success("Usuario creado exitosamente. Debe cambiar su contraseña.", null));
    }

    // === 2FA ENDPOINTS con OTP propio ===

    @PostMapping("/2fa/request-setup")
    public ResponseEntity<ApiResponse<Void>> requestTwoFactorSetup(
            @AuthenticationPrincipal UserDetails userDetails) {
        
        UUID userId = ((com.academic_system.security.CustomUserDetails) userDetails).getUserId();
        return ResponseEntity.ok(authService.requestTwoFactorSetup(userId));
    }

    @PostMapping("/2fa/enable")
    public ResponseEntity<ApiResponse<Void>> enableTwoFactor(
            @RequestParam String code,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        UUID userId = ((com.academic_system.security.CustomUserDetails) userDetails).getUserId();
        return ResponseEntity.ok(authService.enableTwoFactor(userId, code));
    }

    @PostMapping("/2fa/disable")
    public ResponseEntity<ApiResponse<Void>> disableTwoFactor(
            @RequestParam String password,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        UUID userId = ((com.academic_system.security.CustomUserDetails) userDetails).getUserId();
        return ResponseEntity.ok(authService.disableTwoFactor(userId, password));
    }
}
