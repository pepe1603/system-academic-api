package com.academic_system.controller.cpanel;

import com.academic_system.dto.auth.ApiResponse;
import com.academic_system.dto.cpanel.CreateRetakeExamRequest;
import com.academic_system.dto.cpanel.RetakeExamDTO;
import com.academic_system.dto.cpanel.UpdateRetakeExamRequest;
import com.academic_system.service.RetakeExamService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/retake-exams")
@RequiredArgsConstructor
public class RetakeExamController {

    private final RetakeExamService retakeExamService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<RetakeExamDTO>>> getAllRetakeExams(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        var pageable = PageRequest.of(page, size);
        var result = retakeExamService.getAllRetakeExams(pageable);
        return ResponseEntity.ok(ApiResponse.success(result.getContent()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<RetakeExamDTO>> getRetakeExam(@PathVariable String id) {
        Optional<RetakeExamDTO> retakeExam = retakeExamService.getRetakeExamById(id);
        return retakeExam.map(r -> ResponseEntity.ok(ApiResponse.success(r)))
                .orElseGet(() -> ResponseEntity.ok(ApiResponse.error("Registro de retake no encontrado")));
    }

    @GetMapping("/by-student/{studentId}")
    public ResponseEntity<ApiResponse<List<RetakeExamDTO>>> getRetakeExamsByStudent(
            @PathVariable String studentId) {
        var result = retakeExamService.getRetakeExamsByStudent(studentId);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @GetMapping("/by-course/{courseId}")
    public ResponseEntity<ApiResponse<List<RetakeExamDTO>>> getRetakeExamsByCourse(
            @PathVariable String courseId) {
        var result = retakeExamService.getRetakeExamsByCourse(courseId);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @GetMapping("/by-semester/{semesterId}")
    public ResponseEntity<ApiResponse<List<RetakeExamDTO>>> getRetakeExamsBySemester(
            @PathVariable String semesterId) {
        var result = retakeExamService.getRetakeExamsBySemester(semesterId);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<RetakeExamDTO>> createRetakeExam(
            @Valid @RequestBody CreateRetakeExamRequest request) {
        RetakeExamDTO retakeExam = retakeExamService.createRetakeExam(request);
        return ResponseEntity.ok(ApiResponse.success("Registro de retake creado", retakeExam));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<RetakeExamDTO>> updateRetakeExam(
            @PathVariable String id,
            @Valid @RequestBody UpdateRetakeExamRequest request) {
        RetakeExamDTO retakeExam = retakeExamService.updateRetakeExam(id, request);
        return ResponseEntity.ok(ApiResponse.success("Registro de retake actualizado", retakeExam));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteRetakeExam(@PathVariable String id) {
        retakeExamService.deleteRetakeExam(id);
        return ResponseEntity.ok(ApiResponse.success("Registro de retake eliminado", null));
    }

    @GetMapping("/deleted")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<RetakeExamDTO>>> getDeletedRetakeExams(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        var pageable = PageRequest.of(page, size);
        var result = retakeExamService.getDeletedRetakeExams(pageable);
        return ResponseEntity.ok(ApiResponse.success(result));
    }
}
