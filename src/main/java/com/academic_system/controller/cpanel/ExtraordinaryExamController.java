package com.academic_system.controller.cpanel;

import com.academic_system.dto.auth.ApiResponse;
import com.academic_system.dto.cpanel.CreateExtraordinaryExamRequest;
import com.academic_system.dto.cpanel.ExtraordinaryExamDTO;
import com.academic_system.dto.cpanel.UpdateExtraordinaryExamRequest;
import com.academic_system.service.ExtraordinaryExamService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/extraordinary-exams")
@RequiredArgsConstructor
public class ExtraordinaryExamController {

    private final ExtraordinaryExamService extraordinaryExamService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<ExtraordinaryExamDTO>>> getAllExams(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        var pageable = PageRequest.of(page, size);
        var result = extraordinaryExamService.getAllExams(pageable);
        return ResponseEntity.ok(ApiResponse.success(result.getContent()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ExtraordinaryExamDTO>> getExam(@PathVariable String id) {
        Optional<ExtraordinaryExamDTO> exam = extraordinaryExamService.getExamById(id);
        return exam.map(e -> ResponseEntity.ok(ApiResponse.success(e)))
                .orElseGet(() -> ResponseEntity.ok(ApiResponse.error("Examen extraordinario no encontrado")));
    }

    @GetMapping("/by-student/{studentId}")
    public ResponseEntity<ApiResponse<List<ExtraordinaryExamDTO>>> getExamsByStudent(
            @PathVariable String studentId) {
        var result = extraordinaryExamService.getExamsByStudent(studentId);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @GetMapping("/by-course/{courseId}")
    public ResponseEntity<ApiResponse<List<ExtraordinaryExamDTO>>> getExamsByCourse(
            @PathVariable String courseId) {
        var result = extraordinaryExamService.getExamsByCourse(courseId);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ExtraordinaryExamDTO>> createExam(
            @Valid @RequestBody CreateExtraordinaryExamRequest request) {
        ExtraordinaryExamDTO exam = extraordinaryExamService.createExam(request);
        return ResponseEntity.ok(ApiResponse.success("Examen extraordinario creado", exam));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ExtraordinaryExamDTO>> updateExam(
            @PathVariable String id,
            @Valid @RequestBody UpdateExtraordinaryExamRequest request) {
        ExtraordinaryExamDTO exam = extraordinaryExamService.updateExam(id, request);
        return ResponseEntity.ok(ApiResponse.success("Examen extraordinario actualizado", exam));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteExam(@PathVariable String id) {
        extraordinaryExamService.deleteExam(id);
        return ResponseEntity.ok(ApiResponse.success("Examen extraordinario eliminado", null));
    }

    @GetMapping("/deleted")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<ExtraordinaryExamDTO>>> getDeletedExams(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        var pageable = PageRequest.of(page, size);
        var result = extraordinaryExamService.getDeletedExams(pageable);
        return ResponseEntity.ok(ApiResponse.success(result));
    }
}
