package com.academic_system.controller.cpanel;

import com.academic_system.dto.auth.ApiResponse;
import com.academic_system.dto.cpanel.EnrichedProfileDTO;
import com.academic_system.dto.cpanel.UpdateProfileRequest;
import com.academic_system.dto.cpanel.UserProfileDTO;
import com.academic_system.security.CustomUserDetails;
import com.academic_system.service.ProfileMigrationService;
import com.academic_system.service.UserProfileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class UserProfileController {

    private final UserProfileService userProfileService;
    private final ProfileMigrationService profileMigrationService;

    @GetMapping("/profile/me")
    public ResponseEntity<ApiResponse<EnrichedProfileDTO>> getMyProfile(
            @AuthenticationPrincipal CustomUserDetails currentUser) {
        Optional<EnrichedProfileDTO> profile = userProfileService.getEnrichedProfileByUserId(currentUser.getUserId().toString());
        return profile.map(p -> ResponseEntity.ok(ApiResponse.success(p)))
                .orElseGet(() -> ResponseEntity.ok(ApiResponse.success(null)));
    }

    @PutMapping("/profile/me")
    public ResponseEntity<ApiResponse<UserProfileDTO>> updateMyProfile(
            @AuthenticationPrincipal CustomUserDetails currentUser,
            @Valid @RequestBody UpdateProfileRequest request) {
        UserProfileDTO profile = userProfileService.createOrUpdateProfile(currentUser.getUserId().toString(), request);
        return ResponseEntity.ok(ApiResponse.success("Perfil actualizado", profile));
    }

    @GetMapping("/users/{id}/profile")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<EnrichedProfileDTO>> getUserProfile(@PathVariable String id) {
        Optional<EnrichedProfileDTO> profile = userProfileService.getEnrichedProfileByUserId(id);
        return profile.map(p -> ResponseEntity.ok(ApiResponse.success(p)))
                .orElseGet(() -> ResponseEntity.ok(ApiResponse.success(null)));
    }

    @PutMapping("/users/{id}/profile")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<UserProfileDTO>> updateUserProfile(
            @PathVariable String id,
            @Valid @RequestBody UpdateProfileRequest request) {
        UserProfileDTO profile = userProfileService.createOrUpdateProfile(id, request);
        return ResponseEntity.ok(ApiResponse.success("Perfil actualizado", profile));
    }

    @GetMapping("/profile/search")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<EnrichedProfileDTO>> searchProfileByCurp(@RequestParam String curp) {
        Optional<EnrichedProfileDTO> profile = userProfileService.getEnrichedProfileByCurp(curp);
        return profile.map(p -> ResponseEntity.ok(ApiResponse.success(p)))
                .orElseGet(() -> ResponseEntity.ok(ApiResponse.error("Perfil no encontrado")));
    }

    @GetMapping("/profile/me/academic-history")
    public ResponseEntity<ApiResponse<Object>> getMyAcademicHistory(
            @AuthenticationPrincipal CustomUserDetails currentUser) {
        Object history = userProfileService.getAcademicHistory(currentUser.getUserId().toString());
        return ResponseEntity.ok(ApiResponse.success(history));
    }

    @PostMapping("/admin/migrate-profiles")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<String>> migrateProfiles() {
        try {
            profileMigrationService.migrateExistingProfiles();
            return ResponseEntity.ok(ApiResponse.success("Migración de perfiles completada exitosamente"));
        } catch (Exception e) {
            return ResponseEntity.ok(ApiResponse.error("Error en migración: " + e.getMessage()));
        }
    }
}
