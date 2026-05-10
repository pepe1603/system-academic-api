package com.academic_system.controller.cpanel;

import com.academic_system.dto.auth.ApiResponse;
import com.academic_system.dto.cpanel.AttendanceDTO;
import com.academic_system.dto.cpanel.CreateAttendanceRequest;
import com.academic_system.dto.cpanel.UpdateAttendanceRequest;
import com.academic_system.service.AttendanceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/attendances")
@RequiredArgsConstructor
public class AttendanceController {

    private final AttendanceService attendanceService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<AttendanceDTO>>> getAllAttendances(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        var pageable = PageRequest.of(page, size);
        var result = attendanceService.getAllAttendances(pageable);
        return ResponseEntity.ok(ApiResponse.success(result.getContent()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<AttendanceDTO>> getAttendance(@PathVariable String id) {
        Optional<AttendanceDTO> attendance = attendanceService.getAttendanceById(id);
        return attendance.map(a -> ResponseEntity.ok(ApiResponse.success(a)))
                .orElseGet(() -> ResponseEntity.ok(ApiResponse.error("Registro de asistencia no encontrado")));
    }

    @GetMapping("/by-enrollment/{enrollmentId}")
    public ResponseEntity<ApiResponse<List<AttendanceDTO>>> getAttendancesByEnrollment(
            @PathVariable String enrollmentId) {
        var result = attendanceService.getAttendancesByEnrollment(enrollmentId);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<AttendanceDTO>> createAttendance(
            @Valid @RequestBody CreateAttendanceRequest request) {
        AttendanceDTO attendance = attendanceService.createAttendance(request);
        return ResponseEntity.ok(ApiResponse.success("Asistencia registrada", attendance));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<AttendanceDTO>> updateAttendance(
            @PathVariable String id,
            @Valid @RequestBody UpdateAttendanceRequest request) {
        AttendanceDTO attendance = attendanceService.updateAttendance(id, request);
        return ResponseEntity.ok(ApiResponse.success("Asistencia actualizada", attendance));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteAttendance(@PathVariable String id) {
        attendanceService.deleteAttendance(id);
        return ResponseEntity.ok(ApiResponse.success("Asistencia eliminada", null));
    }

    @GetMapping("/deleted")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<AttendanceDTO>>> getDeletedAttendances(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        var pageable = PageRequest.of(page, size);
        var result = attendanceService.getDeletedAttendances(pageable);
        return ResponseEntity.ok(ApiResponse.success(result));
    }
}
