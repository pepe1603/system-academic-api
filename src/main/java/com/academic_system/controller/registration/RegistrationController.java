package com.academic_system.controller.registration;

import com.academic_system.dto.auth.ApiResponse;
import com.academic_system.dto.registration.*;
import com.academic_system.service.registration.RegistrationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/registration")
@RequiredArgsConstructor
public class RegistrationController {

    private final RegistrationService registrationService;

    @PostMapping("/init")
    public ResponseEntity<ApiResponse<RegistrationRequestDTO>> initRegistration(
            @Valid @RequestBody RegistrationInitRequest request) {
        RegistrationRequestDTO result = registrationService.initRegistration(request);
        return ResponseEntity.ok(ApiResponse.success("Código de verificación enviado", result));
    }

    @PostMapping("/verify")
    public ResponseEntity<ApiResponse<RegistrationRequestDTO>> verifyRegistration(
            @Valid @RequestBody RegistrationVerifyRequest request) {
        RegistrationRequestDTO result = registrationService.verifyRegistration(request);
        return ResponseEntity.ok(ApiResponse.success("Registro completado", result));
    }

    @PostMapping("/verify-email")
    public ResponseEntity<ApiResponse<Void>> verifyEmail(
            @RequestParam String userId,
            @RequestParam String code) {
        registrationService.verifyEmail(userId, code);
        return ResponseEntity.ok(ApiResponse.success("Email verificado", null));
    }
}