package com.academic_system.controller.cpanel;

import com.academic_system.dto.auth.ApiResponse;
import com.academic_system.dto.cpanel.EnrichedProfileDTO;
import com.academic_system.dto.cpanel.UpdateProfileRequest;
import com.academic_system.dto.cpanel.UserProfileDTO;
import com.academic_system.security.CustomUserDetails;
import com.academic_system.service.UserProfileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class UserProfileController {

    private final UserProfileService userProfileService;

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

    @PostMapping("/profile/me/picture")
    public ResponseEntity<ApiResponse<UserProfileDTO>> uploadProfilePicture(
            @AuthenticationPrincipal CustomUserDetails currentUser,
            @RequestParam("file") MultipartFile file) throws IOException {
        String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
        String uploadDir = "uploads/profile-pictures";
        Path uploadPath = Paths.get(uploadDir);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }
        Path filePath = uploadPath.resolve(fileName);
        Files.copy(file.getInputStream(), filePath);
        String fileUrl = "/" + uploadDir + "/" + fileName;
        UpdateProfileRequest request = new UpdateProfileRequest();
        request.setProfilePictureUrl(fileUrl);
        UserProfileDTO profile = userProfileService.createOrUpdateProfile(currentUser.getUserId().toString(), request);
        return ResponseEntity.ok(ApiResponse.success("Foto de perfil actualizada", profile));
    }
}
