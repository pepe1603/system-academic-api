package com.academic_system.controller.cpanel;

import com.academic_system.dto.auth.ApiResponse;
import com.academic_system.dto.cpanel.CreateUserRequest;
import com.academic_system.dto.cpanel.UserDTO;
import com.academic_system.security.CustomUserDetails;
import com.academic_system.service.UserSecurityService;
import com.academic_system.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class UserController {

    private final UserService userService;
    private final UserSecurityService userSecurityService;

    @GetMapping
    public ResponseEntity<ApiResponse<Page<UserDTO>>> getAllUsers(Pageable pageable) {
        Page<UserDTO> page = userService.getAllUsers(pageable);
        return ResponseEntity.ok(ApiResponse.success(page));
    }

    @GetMapping("/deleted")
    public ResponseEntity<ApiResponse<Page<UserDTO>>> getDeletedUsers(Pageable pageable) {
        Page<UserDTO> page = userService.getDeletedUsers(pageable);
        return ResponseEntity.ok(ApiResponse.success(page));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<UserDTO>> getUser(@PathVariable String id) {
        UserDTO user = userService.getUserById(id);
        return ResponseEntity.ok(ApiResponse.success(user));
    }

    @GetMapping("/roles/permissions")
    public ResponseEntity<ApiResponse<List<String>>> getPermissionsByRole(@RequestParam String roleName) {
        Set<String> permissions = userService.getPermissionsByRole(roleName);
        return ResponseEntity.ok(ApiResponse.success(
                permissions.stream().collect(Collectors.toList())
        ));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<UserDTO>> createUser(@Valid @RequestBody CreateUserRequest request) {
        UserDTO user = userService.createUser(request);
        return ResponseEntity.ok(ApiResponse.success("Usuario creado", user));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<UserDTO>> updateUser(
            @PathVariable String id,
            @RequestBody UpdateUserRequest request) {
        UserDTO user = userService.updateUser(id, request.getIsActive(), request.getRoles(), request.getMustChangePassword());
        return ResponseEntity.ok(ApiResponse.success("Usuario actualizado", user));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteUser(
            @PathVariable String id,
            @AuthenticationPrincipal CustomUserDetails currentUser) {
        userService.deleteUser(id, currentUser.getUserId().toString());
        return ResponseEntity.ok(ApiResponse.success("Usuario eliminado", null));
    }

    @DeleteMapping("/{id}/sessions")
    public ResponseEntity<ApiResponse<Void>> revokeUserSessions(@PathVariable String id) {
        return ResponseEntity.ok(userService.revokeAllSessions(id));
    }

    @PutMapping("/{id}/unlock")
    public ResponseEntity<ApiResponse<Void>> unlockUser(@PathVariable String id) {
        userSecurityService.unlockUser(java.util.UUID.fromString(id));
        return ResponseEntity.ok(ApiResponse.success("Usuario desbloqueado", null));
    }

    @PutMapping("/{id}/lock")
    public ResponseEntity<ApiResponse<Void>> lockUser(@PathVariable String id) {
        userSecurityService.lockUser(java.util.UUID.fromString(id));
        return ResponseEntity.ok(ApiResponse.success("Usuario bloqueado", null));
    }

    @PutMapping("/{id}/ban")
    public ResponseEntity<ApiResponse<Void>> banUser(@PathVariable String id) {
        userService.banUser(id);
        return ResponseEntity.ok(ApiResponse.success("Usuario baneado (desactivado)", null));
    }

    @lombok.Data
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class UpdateUserRequest {
        private Boolean isActive;
        private Set<String> roles;
        private Boolean mustChangePassword;
    }
}