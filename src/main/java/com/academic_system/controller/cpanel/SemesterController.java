package com.academic_system.controller.cpanel;

import com.academic_system.dto.auth.ApiResponse;
import com.academic_system.dto.cpanel.CreateSemesterRequest;
import com.academic_system.dto.cpanel.SemesterDTO;
import com.academic_system.dto.cpanel.UpdateSemesterRequest;
import com.academic_system.service.SemesterService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/semesters")
@RequiredArgsConstructor
public class SemesterController {

    private final SemesterService semesterService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<SemesterDTO>>> getAllSemesters(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        var pageable = PageRequest.of(page, size);
        var result = semesterService.getAllSemesters(pageable);
        return ResponseEntity.ok(ApiResponse.success(result.getContent()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<SemesterDTO>> getSemester(@PathVariable String id) {
        Optional<SemesterDTO> semester = semesterService.getSemesterById(id);
        return semester.map(s -> ResponseEntity.ok(ApiResponse.success(s)))
                .orElseGet(() -> ResponseEntity.ok(ApiResponse.error("Semestre no encontrado")));
    }

    @GetMapping("/by-study-plan/{studyPlanId}")
    public ResponseEntity<ApiResponse<List<SemesterDTO>>> getSemestersByStudyPlan(
            @PathVariable String studyPlanId) {
        var result = semesterService.getSemestersByStudyPlan(studyPlanId);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<SemesterDTO>> createSemester(
            @Valid @RequestBody CreateSemesterRequest request) {
        SemesterDTO semester = semesterService.createSemester(request);
        return ResponseEntity.ok(ApiResponse.success("Semestre creado", semester));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<SemesterDTO>> updateSemester(
            @PathVariable String id,
            @Valid @RequestBody UpdateSemesterRequest request) {
        SemesterDTO semester = semesterService.updateSemester(id, request);
        return ResponseEntity.ok(ApiResponse.success("Semestre actualizado", semester));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteSemester(@PathVariable String id) {
        semesterService.deleteSemester(id);
        return ResponseEntity.ok(ApiResponse.success("Semestre eliminado", null));
    }

    @GetMapping("/deleted")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<SemesterDTO>>> getDeletedSemesters(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        var pageable = PageRequest.of(page, size);
        var result = semesterService.getDeletedSemesters(pageable);
        return ResponseEntity.ok(ApiResponse.success(result));
    }
}
