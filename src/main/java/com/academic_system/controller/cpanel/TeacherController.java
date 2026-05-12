package com.academic_system.controller.cpanel;

import com.academic_system.dto.auth.ApiResponse;
import com.academic_system.dto.cpanel.CreateTeacherRequest;
import com.academic_system.dto.cpanel.TeacherDTO;
import com.academic_system.dto.cpanel.UpdateTeacherRequest;
import com.academic_system.service.TeacherService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/teachers")
@RequiredArgsConstructor
public class TeacherController {

    private final TeacherService teacherService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<TeacherDTO>>> getAllTeachers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        var pageable = PageRequest.of(page, size);
        var result = teacherService.getAllTeachers(pageable);
        return ResponseEntity.ok(ApiResponse.success(result.getContent()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<TeacherDTO>> getTeacher(@PathVariable String id) {
        Optional<TeacherDTO> teacher = teacherService.getTeacherById(id);
        return teacher.map(t -> ResponseEntity.ok(ApiResponse.success(t)))
                .orElseGet(() -> ResponseEntity.ok(ApiResponse.error("Docente no encontrado")));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<TeacherDTO>> createTeacher(
            @Valid @RequestBody CreateTeacherRequest request) {
        TeacherDTO teacher = teacherService.createTeacher(request);
        return ResponseEntity.ok(ApiResponse.success("Docente creado", teacher));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<TeacherDTO>> updateTeacher(
            @PathVariable String id,
            @Valid @RequestBody UpdateTeacherRequest request) {
        TeacherDTO teacher = teacherService.updateTeacher(id, request);
        return ResponseEntity.ok(ApiResponse.success("Docente actualizado", teacher));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteTeacher(@PathVariable String id) {
        teacherService.deleteTeacher(id);
        return ResponseEntity.ok(ApiResponse.success("Docente eliminado", null));
    }

    @GetMapping("/deleted")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<TeacherDTO>>> getDeletedTeachers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        var pageable = PageRequest.of(page, size);
        var result = teacherService.getDeletedTeachers(pageable);
        return ResponseEntity.ok(ApiResponse.success(result));
    }
}
