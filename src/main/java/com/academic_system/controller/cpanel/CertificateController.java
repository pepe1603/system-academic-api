package com.academic_system.controller.cpanel;

import com.academic_system.dto.auth.ApiResponse;
import com.academic_system.dto.cpanel.CertificateDTO;
import com.academic_system.dto.cpanel.CreateCertificateRequest;
import com.academic_system.dto.cpanel.UpdateCertificateRequest;
import com.academic_system.service.CertificateService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/certificates")
@RequiredArgsConstructor
public class CertificateController {

    private final CertificateService certificateService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<CertificateDTO>>> getAllCertificates(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        var pageable = PageRequest.of(page, size);
        var result = certificateService.getAllCertificates(pageable);
        return ResponseEntity.ok(ApiResponse.success(result.getContent()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CertificateDTO>> getCertificate(@PathVariable String id) {
        Optional<CertificateDTO> certificate = certificateService.getCertificateById(id);
        return certificate.map(c -> ResponseEntity.ok(ApiResponse.success(c)))
                .orElseGet(() -> ResponseEntity.ok(ApiResponse.error("Certificado no encontrado")));
    }

    @GetMapping("/by-student/{studentId}")
    public ResponseEntity<ApiResponse<List<CertificateDTO>>> getCertificatesByStudent(
            @PathVariable String studentId) {
        var result = certificateService.getCertificatesByStudent(studentId);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<CertificateDTO>> createCertificate(
            @Valid @RequestBody CreateCertificateRequest request) {
        CertificateDTO certificate = certificateService.createCertificate(request);
        return ResponseEntity.ok(ApiResponse.success("Certificado creado", certificate));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<CertificateDTO>> updateCertificate(
            @PathVariable String id,
            @Valid @RequestBody UpdateCertificateRequest request) {
        CertificateDTO certificate = certificateService.updateCertificate(id, request);
        return ResponseEntity.ok(ApiResponse.success("Certificado actualizado", certificate));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteCertificate(@PathVariable String id) {
        certificateService.deleteCertificate(id);
        return ResponseEntity.ok(ApiResponse.success("Certificado eliminado", null));
    }

    @GetMapping("/deleted")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<CertificateDTO>>> getDeletedCertificates(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        var pageable = PageRequest.of(page, size);
        var result = certificateService.getDeletedCertificates(pageable);
        return ResponseEntity.ok(ApiResponse.success(result));
    }
}
