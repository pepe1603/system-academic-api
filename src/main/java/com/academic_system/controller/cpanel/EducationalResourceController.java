package com.academic_system.controller.cpanel;

import com.academic_system.dto.auth.ApiResponse;
import com.academic_system.dto.cpanel.CreateEducationalResourceRequest;
import com.academic_system.dto.cpanel.EducationalResourceDTO;
import com.academic_system.dto.cpanel.UpdateEducationalResourceRequest;
import com.academic_system.service.EducationalResourceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/educational-resources")
@RequiredArgsConstructor
public class EducationalResourceController {

    private final EducationalResourceService educationalResourceService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<EducationalResourceDTO>>> getAllResources(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        var pageable = PageRequest.of(page, size);
        var result = educationalResourceService.getAllResources(pageable);
        return ResponseEntity.ok(ApiResponse.success(result.getContent()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<EducationalResourceDTO>> getResource(@PathVariable String id) {
        Optional<EducationalResourceDTO> resource = educationalResourceService.getResourceById(id);
        return resource.map(r -> ResponseEntity.ok(ApiResponse.success(r)))
                .orElseGet(() -> ResponseEntity.ok(ApiResponse.error("Recurso no encontrado")));
    }

    @GetMapping("/by-course/{courseId}")
    public ResponseEntity<ApiResponse<List<EducationalResourceDTO>>> getResourcesByCourse(
            @PathVariable String courseId) {
        var result = educationalResourceService.getResourcesByCourse(courseId);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<EducationalResourceDTO>> createResource(
            @Valid @RequestBody CreateEducationalResourceRequest request) {
        EducationalResourceDTO resource = educationalResourceService.createResource(request);
        return ResponseEntity.ok(ApiResponse.success("Recurso creado", resource));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<EducationalResourceDTO>> updateResource(
            @PathVariable String id,
            @Valid @RequestBody UpdateEducationalResourceRequest request) {
        EducationalResourceDTO resource = educationalResourceService.updateResource(id, request);
        return ResponseEntity.ok(ApiResponse.success("Recurso actualizado", resource));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteResource(@PathVariable String id) {
        educationalResourceService.deleteResource(id);
        return ResponseEntity.ok(ApiResponse.success("Recurso eliminado", null));
    }

    @GetMapping("/deleted")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<EducationalResourceDTO>>> getDeletedResources(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        var pageable = PageRequest.of(page, size);
        var result = educationalResourceService.getDeletedResources(pageable);
        return ResponseEntity.ok(ApiResponse.success(result));
    }
}
