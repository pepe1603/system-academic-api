package com.academic_system.controller.cpanel;

import com.academic_system.dto.auth.ApiResponse;
import com.academic_system.dto.cpanel.AccessAuditDTO;
import com.academic_system.service.AccessAuditService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/access-audit")
@RequiredArgsConstructor
public class AccessAuditController {

    private final AccessAuditService accessAuditService;

    @GetMapping
    public ResponseEntity<ApiResponse<Page<AccessAuditDTO>>> getAllAuditLogs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String userId,
            @RequestParam(required = false) String module,
            @RequestParam(required = false) String action,
            @RequestParam(required = false) Boolean success) {

        var pageable = PageRequest.of(page, size);

        if (userId != null) {
            return ResponseEntity.ok(ApiResponse.success(
                    accessAuditService.getAuditLogsByUserId(userId, pageable)));
        }
        if (module != null) {
            return ResponseEntity.ok(ApiResponse.success(
                    accessAuditService.getAuditLogsByModule(module, pageable)));
        }
        if (action != null) {
            return ResponseEntity.ok(ApiResponse.success(
                    accessAuditService.getAuditLogsByAction(action, pageable)));
        }
        if (success != null) {
            return ResponseEntity.ok(ApiResponse.success(
                    accessAuditService.getAuditLogsBySuccess(success, pageable)));
        }

        return ResponseEntity.ok(ApiResponse.success(
                accessAuditService.getAllAuditLogs(pageable)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<AccessAuditDTO>> getAuditLog(@PathVariable String id) {
        Optional<AccessAuditDTO> audit = accessAuditService.getAuditLogById(id);
        return audit.map(a -> ResponseEntity.ok(ApiResponse.success(a)))
                .orElseGet(() -> ResponseEntity.ok(ApiResponse.error("Registro de auditoría no encontrado")));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteAuditLog(@PathVariable String id) {
        accessAuditService.deleteAuditLog(id);
        return ResponseEntity.ok(ApiResponse.success("Registro de auditoría eliminado", null));
    }
}
