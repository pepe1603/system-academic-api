package com.academic_system.controller.cpanel;

import com.academic_system.dto.auth.ApiResponse;
import com.academic_system.dto.cpanel.AcademicPeriodDTO;
import com.academic_system.dto.cpanel.CreateAcademicPeriodRequest;
import com.academic_system.dto.cpanel.UpdateAcademicPeriodRequest;
import com.academic_system.service.AcademicPeriodService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/academic-periods")
@RequiredArgsConstructor
public class AcademicPeriodController {

    private final AcademicPeriodService academicPeriodService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<AcademicPeriodDTO>>> getAllAcademicPeriods(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        var pageable = PageRequest.of(page, size);
        var result = academicPeriodService.getAllAcademicPeriods(pageable);
        return ResponseEntity.ok(ApiResponse.success(result.getContent()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<AcademicPeriodDTO>> getAcademicPeriod(@PathVariable String id) {
        Optional<AcademicPeriodDTO> academicPeriod = academicPeriodService.getAcademicPeriodById(id);
        return academicPeriod.map(ap -> ResponseEntity.ok(ApiResponse.success(ap)))
                .orElseGet(() -> ResponseEntity.ok(ApiResponse.error("Período académico no encontrado")));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<AcademicPeriodDTO>> createAcademicPeriod(
            @Valid @RequestBody CreateAcademicPeriodRequest request) {
        AcademicPeriodDTO academicPeriod = academicPeriodService.createAcademicPeriod(request);
        return ResponseEntity.ok(ApiResponse.success("Período académico creado", academicPeriod));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<AcademicPeriodDTO>> updateAcademicPeriod(
            @PathVariable String id,
            @Valid @RequestBody UpdateAcademicPeriodRequest request) {
        AcademicPeriodDTO academicPeriod = academicPeriodService.updateAcademicPeriod(id, request);
        return ResponseEntity.ok(ApiResponse.success("Período académico actualizado", academicPeriod));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteAcademicPeriod(@PathVariable String id) {
        academicPeriodService.deleteAcademicPeriod(id);
        return ResponseEntity.ok(ApiResponse.success("Período académico eliminado", null));
    }

    @GetMapping("/deleted")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<AcademicPeriodDTO>>> getDeletedAcademicPeriods(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        var pageable = PageRequest.of(page, size);
        var result = academicPeriodService.getDeletedAcademicPeriods(pageable);
        return ResponseEntity.ok(ApiResponse.success(result));
    }
}
