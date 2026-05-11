package com.academic_system.controller.cpanel;

import com.academic_system.dto.auth.ApiResponse;
import com.academic_system.dto.cpanel.CreateReportCardRequest;
import com.academic_system.dto.cpanel.ReportCardDTO;
import com.academic_system.dto.cpanel.UpdateReportCardRequest;
import com.academic_system.service.ReportCardService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/report-cards")
@RequiredArgsConstructor
public class ReportCardController {

    private final ReportCardService reportCardService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<ReportCardDTO>>> getAllReportCards(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        var pageable = PageRequest.of(page, size);
        var result = reportCardService.getAllReportCards(pageable);
        return ResponseEntity.ok(ApiResponse.success(result.getContent()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ReportCardDTO>> getReportCard(@PathVariable String id) {
        Optional<ReportCardDTO> reportCard = reportCardService.getReportCardById(id);
        return reportCard.map(rc -> ResponseEntity.ok(ApiResponse.success(rc)))
                .orElseGet(() -> ResponseEntity.ok(ApiResponse.error("Boleta no encontrada")));
    }

    @GetMapping("/by-student/{studentId}")
    public ResponseEntity<ApiResponse<List<ReportCardDTO>>> getReportCardsByStudent(
            @PathVariable String studentId) {
        var result = reportCardService.getReportCardsByStudent(studentId);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ReportCardDTO>> createReportCard(
            @Valid @RequestBody CreateReportCardRequest request) {
        ReportCardDTO reportCard = reportCardService.createReportCard(request);
        return ResponseEntity.ok(ApiResponse.success("Boleta creada", reportCard));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ReportCardDTO>> updateReportCard(
            @PathVariable String id,
            @Valid @RequestBody UpdateReportCardRequest request) {
        ReportCardDTO reportCard = reportCardService.updateReportCard(id, request);
        return ResponseEntity.ok(ApiResponse.success("Boleta actualizada", reportCard));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteReportCard(@PathVariable String id) {
        reportCardService.deleteReportCard(id);
        return ResponseEntity.ok(ApiResponse.success("Boleta eliminada", null));
    }

    @GetMapping("/deleted")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<ReportCardDTO>>> getDeletedReportCards(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        var pageable = PageRequest.of(page, size);
        var result = reportCardService.getDeletedReportCards(pageable);
        return ResponseEntity.ok(ApiResponse.success(result));
    }
}
