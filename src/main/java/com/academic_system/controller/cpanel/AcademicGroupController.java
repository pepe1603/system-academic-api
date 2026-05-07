package com.academic_system.controller.cpanel;

import com.academic_system.dto.auth.ApiResponse;
import com.academic_system.dto.cpanel.AcademicGroupDTO;
import com.academic_system.dto.cpanel.CreateAcademicGroupRequest;
import com.academic_system.dto.cpanel.UpdateAcademicGroupRequest;
import com.academic_system.service.AcademicGroupService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/academic-groups")
@RequiredArgsConstructor
public class AcademicGroupController {

    private final AcademicGroupService academicGroupService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<AcademicGroupDTO>>> getAllAcademicGroups(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        var pageable = PageRequest.of(page, size);
        var result = academicGroupService.getAllAcademicGroups(pageable);
        return ResponseEntity.ok(ApiResponse.success(result.getContent()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<AcademicGroupDTO>> getAcademicGroup(@PathVariable String id) {
        Optional<AcademicGroupDTO> group = academicGroupService.getAcademicGroupById(id);
        return group.map(g -> ResponseEntity.ok(ApiResponse.success(g)))
                .orElseGet(() -> ResponseEntity.ok(ApiResponse.error("Grupo académico no encontrado")));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<AcademicGroupDTO>> createAcademicGroup(
            @Valid @RequestBody CreateAcademicGroupRequest request) {
        AcademicGroupDTO group = academicGroupService.createAcademicGroup(request);
        return ResponseEntity.ok(ApiResponse.success("Grupo académico creado", group));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<AcademicGroupDTO>> updateAcademicGroup(
            @PathVariable String id,
            @Valid @RequestBody UpdateAcademicGroupRequest request) {
        AcademicGroupDTO group = academicGroupService.updateAcademicGroup(id, request);
        return ResponseEntity.ok(ApiResponse.success("Grupo académico actualizado", group));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteAcademicGroup(@PathVariable String id) {
        academicGroupService.deleteAcademicGroup(id);
        return ResponseEntity.ok(ApiResponse.success("Grupo académico eliminado", null));
    }

    @GetMapping("/deleted")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<AcademicGroupDTO>>> getDeletedAcademicGroups(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        var pageable = PageRequest.of(page, size);
        var result = academicGroupService.getDeletedAcademicGroups(pageable);
        return ResponseEntity.ok(ApiResponse.success(result));
    }
}
