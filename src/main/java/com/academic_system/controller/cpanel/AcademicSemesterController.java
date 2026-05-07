package com.academic_system.controller.cpanel;

import com.academic_system.dto.auth.ApiResponse;
import com.academic_system.dto.cpanel.AcademicSemesterDTO;
import com.academic_system.dto.cpanel.CreateAcademicSemesterRequest;
import com.academic_system.dto.cpanel.UpdateAcademicSemesterRequest;
import com.academic_system.service.AcademicSemesterService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/academic-semesters")
@RequiredArgsConstructor
public class AcademicSemesterController {

    private final AcademicSemesterService academicSemesterService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<AcademicSemesterDTO>>> getAllAcademicSemesters(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        var pageable = PageRequest.of(page, size);
        var result = academicSemesterService.getAllAcademicSemesters(pageable);
        return ResponseEntity.ok(ApiResponse.success(result.getContent()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<AcademicSemesterDTO>> getAcademicSemester(@PathVariable String id) {
        Optional<AcademicSemesterDTO> academicSemester = academicSemesterService.getAcademicSemesterById(id);
        return academicSemester.map(as -> ResponseEntity.ok(ApiResponse.success(as)))
                .orElseGet(() -> ResponseEntity.ok(ApiResponse.error("Semestre académico no encontrado")));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<AcademicSemesterDTO>> createAcademicSemester(
            @Valid @RequestBody CreateAcademicSemesterRequest request) {
        AcademicSemesterDTO academicSemester = academicSemesterService.createAcademicSemester(request);
        return ResponseEntity.ok(ApiResponse.success("Semestre académico creado", academicSemester));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<AcademicSemesterDTO>> updateAcademicSemester(
            @PathVariable String id,
            @Valid @RequestBody UpdateAcademicSemesterRequest request) {
        AcademicSemesterDTO academicSemester = academicSemesterService.updateAcademicSemester(id, request);
        return ResponseEntity.ok(ApiResponse.success("Semestre académico actualizado", academicSemester));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteAcademicSemester(@PathVariable String id) {
        academicSemesterService.deleteAcademicSemester(id);
        return ResponseEntity.ok(ApiResponse.success("Semestre académico eliminado", null));
    }

    @GetMapping("/deleted")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<AcademicSemesterDTO>>> getDeletedAcademicSemesters(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        var pageable = PageRequest.of(page, size);
        var result = academicSemesterService.getDeletedAcademicSemesters(pageable);
        return ResponseEntity.ok(ApiResponse.success(result));
    }
}
