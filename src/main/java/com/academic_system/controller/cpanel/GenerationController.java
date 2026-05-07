package com.academic_system.controller.cpanel;

import com.academic_system.dto.auth.ApiResponse;
import com.academic_system.dto.cpanel.CreateGenerationRequest;
import com.academic_system.dto.cpanel.GenerationDTO;
import com.academic_system.dto.cpanel.UpdateGenerationRequest;
import com.academic_system.service.GenerationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/generations")
@RequiredArgsConstructor
public class GenerationController {

    private final GenerationService generationService;

    @GetMapping
    public ResponseEntity<ApiResponse<java.util.List<GenerationDTO>>> getAllGenerations(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        var pageable = org.springframework.data.domain.PageRequest.of(page, size);
        var result = generationService.getAllGenerations(pageable);
        return ResponseEntity.ok(ApiResponse.success(result.getContent()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<GenerationDTO>> getGeneration(@PathVariable String id) {
        Optional<GenerationDTO> generation = generationService.getGenerationById(id);
        return generation.map(g -> ResponseEntity.ok(ApiResponse.success(g)))
                .orElseGet(() -> ResponseEntity.ok(ApiResponse.error("Generación no encontrada")));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<GenerationDTO>> createGeneration(
            @Valid @RequestBody CreateGenerationRequest request) {
        GenerationDTO generation = generationService.createGeneration(request);
        return ResponseEntity.ok(ApiResponse.success("Generación creada", generation));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<GenerationDTO>> updateGeneration(
            @PathVariable String id,
            @Valid @RequestBody UpdateGenerationRequest request) {
        GenerationDTO generation = generationService.updateGeneration(id, request);
        return ResponseEntity.ok(ApiResponse.success("Generación actualizada", generation));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteGeneration(@PathVariable String id) {
        generationService.deleteGeneration(id);
        return ResponseEntity.ok(ApiResponse.success("Generación eliminada", null));
    }

    @GetMapping("/deleted")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<GenerationDTO>>> getDeletedGenerations(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        var pageable = org.springframework.data.domain.PageRequest.of(page, size);
        var result = generationService.getDeletedGenerations(pageable);
        return ResponseEntity.ok(ApiResponse.success(result));
    }
}
