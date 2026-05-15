package com.academic_system.controller.cpanel;

import com.academic_system.dto.auth.ApiResponse;
import com.academic_system.dto.cpanel.CreateStudentDocumentRequest;
import com.academic_system.dto.cpanel.StudentDocumentDTO;
import com.academic_system.dto.cpanel.UpdateStudentDocumentRequest;
import com.academic_system.service.StudentDocumentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/student-documents")
@RequiredArgsConstructor
public class StudentDocumentController {

    private final StudentDocumentService studentDocumentService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<StudentDocumentDTO>>> getAllDocuments(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        var pageable = PageRequest.of(page, size);
        var result = studentDocumentService.getAllDocuments(pageable);
        return ResponseEntity.ok(ApiResponse.success(result.getContent()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<StudentDocumentDTO>> getDocument(@PathVariable String id) {
        Optional<StudentDocumentDTO> document = studentDocumentService.getDocumentById(id);
        return document.map(d -> ResponseEntity.ok(ApiResponse.success(d)))
                .orElseGet(() -> ResponseEntity.ok(ApiResponse.error("Documento no encontrado")));
    }

    @GetMapping("/by-student/{studentId}")
    public ResponseEntity<ApiResponse<List<StudentDocumentDTO>>> getDocumentsByStudent(
            @PathVariable String studentId) {
        var result = studentDocumentService.getDocumentsByStudent(studentId);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<StudentDocumentDTO>> createDocument(
            @Valid @RequestBody CreateStudentDocumentRequest request) {
        StudentDocumentDTO document = studentDocumentService.createDocument(request);
        return ResponseEntity.ok(ApiResponse.success("Documento creado", document));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<StudentDocumentDTO>> updateDocument(
            @PathVariable String id,
            @Valid @RequestBody UpdateStudentDocumentRequest request) {
        StudentDocumentDTO document = studentDocumentService.updateDocument(id, request);
        return ResponseEntity.ok(ApiResponse.success("Documento actualizado", document));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteDocument(@PathVariable String id) {
        studentDocumentService.deleteDocument(id);
        return ResponseEntity.ok(ApiResponse.success("Documento eliminado", null));
    }

    @GetMapping("/deleted")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<StudentDocumentDTO>>> getDeletedDocuments(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        var pageable = PageRequest.of(page, size);
        var result = studentDocumentService.getDeletedDocuments(pageable);
        return ResponseEntity.ok(ApiResponse.success(result));
    }
}
