package com.academic_system.controller.cpanel;

import com.academic_system.dto.auth.ApiResponse;
import com.academic_system.dto.cpanel.CreateEvaluationTypeRequest;
import com.academic_system.dto.cpanel.EvaluationTypeDTO;
import com.academic_system.dto.cpanel.UpdateEvaluationTypeRequest;
import com.academic_system.service.EvaluationTypeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/evaluation-types")
@RequiredArgsConstructor
public class EvaluationTypeController {

    private final EvaluationTypeService evaluationTypeService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<EvaluationTypeDTO>>> getAllEvaluationTypes(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        var pageable = PageRequest.of(page, size);
        var result = evaluationTypeService.getAllEvaluationTypes(pageable);
        return ResponseEntity.ok(ApiResponse.success(result.getContent()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<EvaluationTypeDTO>> getEvaluationType(@PathVariable String id) {
        Optional<EvaluationTypeDTO> evaluationType = evaluationTypeService.getEvaluationTypeById(id);
        return evaluationType.map(et -> ResponseEntity.ok(ApiResponse.success(et)))
                .orElseGet(() -> ResponseEntity.ok(ApiResponse.error("Tipo de evaluación no encontrado")));
    }

    @GetMapping("/by-course/{courseId}")
    public ResponseEntity<ApiResponse<List<EvaluationTypeDTO>>> getEvaluationTypesByCourse(
            @PathVariable String courseId) {
        var result = evaluationTypeService.getEvaluationTypesByCourse(courseId);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<EvaluationTypeDTO>> createEvaluationType(
            @Valid @RequestBody CreateEvaluationTypeRequest request) {
        EvaluationTypeDTO evaluationType = evaluationTypeService.createEvaluationType(request);
        return ResponseEntity.ok(ApiResponse.success("Tipo de evaluación creado", evaluationType));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<EvaluationTypeDTO>> updateEvaluationType(
            @PathVariable String id,
            @Valid @RequestBody UpdateEvaluationTypeRequest request) {
        EvaluationTypeDTO evaluationType = evaluationTypeService.updateEvaluationType(id, request);
        return ResponseEntity.ok(ApiResponse.success("Tipo de evaluación actualizado", evaluationType));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteEvaluationType(@PathVariable String id) {
        evaluationTypeService.deleteEvaluationType(id);
        return ResponseEntity.ok(ApiResponse.success("Tipo de evaluación eliminado", null));
    }

    @GetMapping("/inactive")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<EvaluationTypeDTO>>> getInactiveEvaluationTypes(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        var pageable = PageRequest.of(page, size);
        var result = evaluationTypeService.getInactiveEvaluationTypes(pageable);
        return ResponseEntity.ok(ApiResponse.success(result));
    }
}
