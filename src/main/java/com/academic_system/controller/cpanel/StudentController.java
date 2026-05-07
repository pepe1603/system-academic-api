package com.academic_system.controller.cpanel;

import com.academic_system.dto.auth.ApiResponse;
import com.academic_system.dto.cpanel.CreateStudentRequest;
import com.academic_system.dto.cpanel.StudentDTO;
import com.academic_system.dto.cpanel.UpdateStudentRequest;
import com.academic_system.service.StudentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/students")
@RequiredArgsConstructor
public class StudentController {

    private final StudentService studentService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<StudentDTO>>> getAllStudents(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        var pageable = PageRequest.of(page, size);
        var result = studentService.getAllStudents(pageable);
        return ResponseEntity.ok(ApiResponse.success(result.getContent()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<StudentDTO>> getStudent(@PathVariable String id) {
        Optional<StudentDTO> student = studentService.getStudentById(id);
        return student.map(s -> ResponseEntity.ok(ApiResponse.success(s)))
                .orElseGet(() -> ResponseEntity.ok(ApiResponse.error("Estudiante no encontrado")));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<StudentDTO>> createStudent(
            @Valid @RequestBody CreateStudentRequest request) {
        StudentDTO student = studentService.createStudent(request);
        return ResponseEntity.ok(ApiResponse.success("Estudiante creado", student));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<StudentDTO>> updateStudent(
            @PathVariable String id,
            @Valid @RequestBody UpdateStudentRequest request) {
        StudentDTO student = studentService.updateStudent(id, request);
        return ResponseEntity.ok(ApiResponse.success("Estudiante actualizado", student));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteStudent(@PathVariable String id) {
        studentService.deleteStudent(id);
        return ResponseEntity.ok(ApiResponse.success("Estudiante eliminado", null));
    }

    @GetMapping("/deleted")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<StudentDTO>>> getDeletedStudents(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        var pageable = PageRequest.of(page, size);
        var result = studentService.getDeletedStudents(pageable);
        return ResponseEntity.ok(ApiResponse.success(result));
    }
}
