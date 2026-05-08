package com.academic_system.controller.cpanel;

import com.academic_system.dto.auth.ApiResponse;
import com.academic_system.dto.cpanel.CreateGradeRequest;
import com.academic_system.dto.cpanel.GradeDTO;
import com.academic_system.dto.cpanel.UpdateGradeRequest;
import com.academic_system.service.GradeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/grades")
@RequiredArgsConstructor
public class GradeController {

    private final GradeService gradeService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<GradeDTO>>> getAllGrades(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        var pageable = PageRequest.of(page, size);
        var result = gradeService.getAllGrades(pageable);
        return ResponseEntity.ok(ApiResponse.success(result.getContent()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<GradeDTO>> getGrade(@PathVariable String id) {
        Optional<GradeDTO> grade = gradeService.getGradeById(id);
        return grade.map(g -> ResponseEntity.ok(ApiResponse.success(g)))
                .orElseGet(() -> ResponseEntity.ok(ApiResponse.error("Calificación no encontrada")));
    }

    @GetMapping("/by-enrollment/{enrollmentId}")
    public ResponseEntity<ApiResponse<List<GradeDTO>>> getGradesByEnrollment(
            @PathVariable String enrollmentId) {
        var result = gradeService.getGradesByEnrollment(enrollmentId);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<GradeDTO>> createGrade(
            @Valid @RequestBody CreateGradeRequest request) {
        GradeDTO grade = gradeService.createGrade(request);
        return ResponseEntity.ok(ApiResponse.success("Calificación creada", grade));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<GradeDTO>> updateGrade(
            @PathVariable String id,
            @Valid @RequestBody UpdateGradeRequest request) {
        GradeDTO grade = gradeService.updateGrade(id, request);
        return ResponseEntity.ok(ApiResponse.success("Calificación actualizada", grade));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteGrade(@PathVariable String id) {
        gradeService.deleteGrade(id);
        return ResponseEntity.ok(ApiResponse.success("Calificación eliminada", null));
    }

    @GetMapping("/deleted")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<GradeDTO>>> getDeletedGrades(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        var pageable = PageRequest.of(page, size);
        var result = gradeService.getDeletedGrades(pageable);
        return ResponseEntity.ok(ApiResponse.success(result));
    }
}
