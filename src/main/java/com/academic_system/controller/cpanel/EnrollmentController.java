package com.academic_system.controller.cpanel;

import com.academic_system.dto.auth.ApiResponse;
import com.academic_system.dto.cpanel.CreateEnrollmentRequest;
import com.academic_system.dto.cpanel.EnrollmentDTO;
import com.academic_system.dto.cpanel.UpdateEnrollmentRequest;
import com.academic_system.service.EnrollmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/enrollments")
@RequiredArgsConstructor
public class EnrollmentController {

    private final EnrollmentService enrollmentService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<EnrollmentDTO>>> getAllEnrollments(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        var pageable = PageRequest.of(page, size);
        var result = enrollmentService.getAllEnrollments(pageable);
        return ResponseEntity.ok(ApiResponse.success(result.getContent()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<EnrollmentDTO>> getEnrollment(@PathVariable String id) {
        Optional<EnrollmentDTO> enrollment = enrollmentService.getEnrollmentById(id);
        return enrollment.map(e -> ResponseEntity.ok(ApiResponse.success(e)))
                .orElseGet(() -> ResponseEntity.ok(ApiResponse.error("Inscripción no encontrada")));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<EnrollmentDTO>> createEnrollment(
            @Valid @RequestBody CreateEnrollmentRequest request) {
        EnrollmentDTO enrollment = enrollmentService.createEnrollment(request);
        return ResponseEntity.ok(ApiResponse.success("Inscripción creada", enrollment));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<EnrollmentDTO>> updateEnrollment(
            @PathVariable String id,
            @Valid @RequestBody UpdateEnrollmentRequest request) {
        EnrollmentDTO enrollment = enrollmentService.updateEnrollment(id, request);
        return ResponseEntity.ok(ApiResponse.success("Inscripción actualizada", enrollment));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteEnrollment(@PathVariable String id) {
        enrollmentService.deleteEnrollment(id);
        return ResponseEntity.ok(ApiResponse.success("Inscripción eliminada", null));
    }

    @GetMapping("/deleted")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<EnrollmentDTO>>> getDeletedEnrollments(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        var pageable = PageRequest.of(page, size);
        var result = enrollmentService.getDeletedEnrollments(pageable);
        return ResponseEntity.ok(ApiResponse.success(result));
    }
}
