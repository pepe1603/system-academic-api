package com.academic_system.controller.cpanel;

import com.academic_system.dto.auth.ApiResponse;
import com.academic_system.dto.cpanel.CreateSystemConfigurationRequest;
import com.academic_system.dto.cpanel.SystemConfigurationDTO;
import com.academic_system.dto.cpanel.UpdateSystemConfigurationRequest;
import com.academic_system.service.SystemConfigurationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/system-configuration")
@RequiredArgsConstructor
public class SystemConfigurationController {

    private final SystemConfigurationService systemConfigurationService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<SystemConfigurationDTO>>> getAllConfigurations(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        var pageable = PageRequest.of(page, size);
        var result = systemConfigurationService.getAllConfigurations(pageable);
        return ResponseEntity.ok(ApiResponse.success(result.getContent()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<SystemConfigurationDTO>> getConfiguration(@PathVariable String id) {
        Optional<SystemConfigurationDTO> config = systemConfigurationService.getConfigurationById(id);
        return config.map(c -> ResponseEntity.ok(ApiResponse.success(c)))
                .orElseGet(() -> ResponseEntity.ok(ApiResponse.error("Configuración no encontrada")));
    }

    @GetMapping("/key/{key}")
    public ResponseEntity<ApiResponse<SystemConfigurationDTO>> getConfigurationByKey(@PathVariable String key) {
        Optional<SystemConfigurationDTO> config = systemConfigurationService.getConfigurationByKey(key);
        return config.map(c -> ResponseEntity.ok(ApiResponse.success(c)))
                .orElseGet(() -> ResponseEntity.ok(ApiResponse.error("Configuración no encontrada")));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<SystemConfigurationDTO>> createConfiguration(
            @Valid @RequestBody CreateSystemConfigurationRequest request) {
        SystemConfigurationDTO config = systemConfigurationService.createConfiguration(request);
        return ResponseEntity.ok(ApiResponse.success("Configuración creada", config));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<SystemConfigurationDTO>> updateConfiguration(
            @PathVariable String id,
            @Valid @RequestBody UpdateSystemConfigurationRequest request) {
        SystemConfigurationDTO config = systemConfigurationService.updateConfiguration(id, request);
        return ResponseEntity.ok(ApiResponse.success("Configuración actualizada", config));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteConfiguration(@PathVariable String id) {
        systemConfigurationService.deleteConfiguration(id);
        return ResponseEntity.ok(ApiResponse.success("Configuración eliminada", null));
    }

    @GetMapping("/deleted")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<SystemConfigurationDTO>>> getDeletedConfigurations(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        var pageable = PageRequest.of(page, size);
        var result = systemConfigurationService.getDeletedConfigurations(pageable);
        return ResponseEntity.ok(ApiResponse.success(result));
    }
}
