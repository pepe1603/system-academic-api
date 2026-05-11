package com.academic_system.controller.cpanel;

import com.academic_system.dto.auth.ApiResponse;
import com.academic_system.dto.cpanel.*;
import com.academic_system.service.ConductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/conduct")
@RequiredArgsConstructor
public class ConductController {

    private final ConductService conductService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<ConductDTO>>> getAllConductRecords(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        var pageable = PageRequest.of(page, size);
        var result = conductService.getAllConductRecords(pageable);
        return ResponseEntity.ok(ApiResponse.success(result.getContent()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ConductDTO>> getConduct(@PathVariable String id) {
        Optional<ConductDTO> conduct = conductService.getConductById(id);
        return conduct.map(c -> ResponseEntity.ok(ApiResponse.success(c)))
                .orElseGet(() -> ResponseEntity.ok(ApiResponse.error("Registro de conducta no encontrado")));
    }

    @GetMapping("/by-enrollment/{enrollmentId}")
    public ResponseEntity<ApiResponse<List<ConductDTO>>> getConductByEnrollment(
            @PathVariable String enrollmentId) {
        var result = conductService.getConductByEnrollment(enrollmentId);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @GetMapping("/by-semester/{semesterId}")
    public ResponseEntity<ApiResponse<List<ConductDTO>>> getConductBySemester(
            @PathVariable String semesterId) {
        var result = conductService.getConductBySemester(semesterId);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ConductDTO>> createConduct(
            @Valid @RequestBody CreateConductRequest request) {
        ConductDTO conduct = conductService.createConduct(request);
        return ResponseEntity.ok(ApiResponse.success("Registro de conducta creado", conduct));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ConductDTO>> updateConduct(
            @PathVariable String id,
            @Valid @RequestBody UpdateConductRequest request) {
        ConductDTO conduct = conductService.updateConduct(id, request);
        return ResponseEntity.ok(ApiResponse.success("Registro de conducta actualizado", conduct));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteConduct(@PathVariable String id) {
        conductService.deleteConduct(id);
        return ResponseEntity.ok(ApiResponse.success("Registro de conducta eliminado", null));
    }

    @GetMapping("/deleted")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<ConductDTO>>> getDeletedConductRecords(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        var pageable = PageRequest.of(page, size);
        var result = conductService.getDeletedConductRecords(pageable);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @GetMapping("/incidents/by-enrollment/{enrollmentId}")
    public ResponseEntity<ApiResponse<List<ConductIncidentDTO>>> getIncidentsByEnrollment(
            @PathVariable String enrollmentId) {
        var result = conductService.getIncidentsByEnrollment(enrollmentId);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @PostMapping("/incidents")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ConductIncidentDTO>> createIncident(
            @Valid @RequestBody CreateConductIncidentRequest request) {
        ConductIncidentDTO incident = conductService.createIncident(request);
        return ResponseEntity.ok(ApiResponse.success("Incidente registrado", incident));
    }

    @PutMapping("/incidents/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ConductIncidentDTO>> updateIncident(
            @PathVariable String id,
            @Valid @RequestBody UpdateConductIncidentRequest request) {
        ConductIncidentDTO incident = conductService.updateIncident(id, request);
        return ResponseEntity.ok(ApiResponse.success("Incidente actualizado", incident));
    }

    @DeleteMapping("/incidents/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteIncident(@PathVariable String id) {
        conductService.deleteIncident(id);
        return ResponseEntity.ok(ApiResponse.success("Incidente eliminado", null));
    }

    @GetMapping("/incidents/deleted")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<ConductIncidentDTO>>> getDeletedIncidents() {
        var result = conductService.getDeletedIncidents();
        return ResponseEntity.ok(ApiResponse.success(result));
    }
}
