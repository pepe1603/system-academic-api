package com.academic_system.controller.cpanel;

import com.academic_system.dto.auth.ApiResponse;
import com.academic_system.dto.cpanel.CreateStudyPlanRequest;
import com.academic_system.dto.cpanel.StudyPlanDTO;
import com.academic_system.dto.cpanel.UpdateStudyPlanRequest;
import com.academic_system.service.StudyPlanService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/study-plans")
@RequiredArgsConstructor
public class StudyPlanController {

    private final StudyPlanService studyPlanService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<StudyPlanDTO>>> getAllStudyPlans(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        var pageable = PageRequest.of(page, size);
        var result = studyPlanService.getAllStudyPlans(pageable);
        return ResponseEntity.ok(ApiResponse.success(result.getContent()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<StudyPlanDTO>> getStudyPlan(@PathVariable String id) {
        Optional<StudyPlanDTO> studyPlan = studyPlanService.getStudyPlanById(id);
        return studyPlan.map(sp -> ResponseEntity.ok(ApiResponse.success(sp)))
                .orElseGet(() -> ResponseEntity.ok(ApiResponse.error("Plan de estudio no encontrado")));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<StudyPlanDTO>> createStudyPlan(
            @Valid @RequestBody CreateStudyPlanRequest request) {
        StudyPlanDTO studyPlan = studyPlanService.createStudyPlan(request);
        return ResponseEntity.ok(ApiResponse.success("Plan de estudio creado", studyPlan));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<StudyPlanDTO>> updateStudyPlan(
            @PathVariable String id,
            @Valid @RequestBody UpdateStudyPlanRequest request) {
        StudyPlanDTO studyPlan = studyPlanService.updateStudyPlan(id, request);
        return ResponseEntity.ok(ApiResponse.success("Plan de estudio actualizado", studyPlan));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteStudyPlan(@PathVariable String id) {
        studyPlanService.deleteStudyPlan(id);
        return ResponseEntity.ok(ApiResponse.success("Plan de estudio eliminado", null));
    }

    @GetMapping("/deleted")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<StudyPlanDTO>>> getDeletedStudyPlans(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        var pageable = PageRequest.of(page, size);
        var result = studyPlanService.getDeletedStudyPlans(pageable);
        return ResponseEntity.ok(ApiResponse.success(result));
    }
}
