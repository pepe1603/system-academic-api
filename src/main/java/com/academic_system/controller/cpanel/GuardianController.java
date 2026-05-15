package com.academic_system.controller.cpanel;

import com.academic_system.dto.auth.ApiResponse;
import com.academic_system.dto.cpanel.CreateGuardianRequest;
import com.academic_system.dto.cpanel.GuardianDTO;
import com.academic_system.dto.cpanel.UpdateGuardianRequest;
import com.academic_system.service.GuardianService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/guardians")
@RequiredArgsConstructor
public class GuardianController {

    private final GuardianService guardianService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<GuardianDTO>>> getAllGuardians(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        var pageable = PageRequest.of(page, size);
        var result = guardianService.getAllGuardians(pageable);
        return ResponseEntity.ok(ApiResponse.success(result.getContent()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<GuardianDTO>> getGuardian(@PathVariable String id) {
        Optional<GuardianDTO> guardian = guardianService.getGuardianById(id);
        return guardian.map(g -> ResponseEntity.ok(ApiResponse.success(g)))
                .orElseGet(() -> ResponseEntity.ok(ApiResponse.error("Tutor no encontrado")));
    }

    @GetMapping("/by-student/{studentId}")
    public ResponseEntity<ApiResponse<List<GuardianDTO>>> getGuardiansByStudent(
            @PathVariable String studentId) {
        var result = guardianService.getGuardiansByStudent(studentId);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<GuardianDTO>> createGuardian(
            @Valid @RequestBody CreateGuardianRequest request) {
        GuardianDTO guardian = guardianService.createGuardian(request);
        return ResponseEntity.ok(ApiResponse.success("Tutor creado", guardian));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<GuardianDTO>> updateGuardian(
            @PathVariable String id,
            @Valid @RequestBody UpdateGuardianRequest request) {
        GuardianDTO guardian = guardianService.updateGuardian(id, request);
        return ResponseEntity.ok(ApiResponse.success("Tutor actualizado", guardian));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteGuardian(@PathVariable String id) {
        guardianService.deleteGuardian(id);
        return ResponseEntity.ok(ApiResponse.success("Tutor eliminado", null));
    }

    @GetMapping("/deleted")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<GuardianDTO>>> getDeletedGuardians(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        var pageable = PageRequest.of(page, size);
        var result = guardianService.getDeletedGuardians(pageable);
        return ResponseEntity.ok(ApiResponse.success(result));
    }
}
