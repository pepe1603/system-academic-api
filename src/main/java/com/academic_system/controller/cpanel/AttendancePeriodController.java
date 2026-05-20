package com.academic_system.controller.cpanel;

import com.academic_system.dto.auth.ApiResponse;
import com.academic_system.dto.cpanel.AttendancePeriodDTO;
import com.academic_system.dto.cpanel.CreateAttendancePeriodRequest;
import com.academic_system.dto.cpanel.UpdateAttendancePeriodRequest;
import com.academic_system.service.AttendancePeriodService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/attendance-periods")
@RequiredArgsConstructor
public class AttendancePeriodController {

    private final AttendancePeriodService attendancePeriodService;

    @GetMapping
    public ResponseEntity<ApiResponse<Page<AttendancePeriodDTO>>> getAllAttendancePeriods(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String enrollmentId,
            @RequestParam(required = false) String academicSemesterId) {

        var pageable = PageRequest.of(page, size);

        if (enrollmentId != null) {
            return ResponseEntity.ok(ApiResponse.success(
                    attendancePeriodService.getByEnrollment(enrollmentId, pageable)));
        }
        if (academicSemesterId != null) {
            return ResponseEntity.ok(ApiResponse.success(
                    attendancePeriodService.getByAcademicSemester(academicSemesterId, pageable)));
        }

        return ResponseEntity.ok(ApiResponse.success(
                attendancePeriodService.getAllAttendancePeriods(pageable)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<AttendancePeriodDTO>> getAttendancePeriod(@PathVariable String id) {
        Optional<AttendancePeriodDTO> period = attendancePeriodService.getAttendancePeriodById(id);
        return period.map(p -> ResponseEntity.ok(ApiResponse.success(p)))
                .orElseGet(() -> ResponseEntity.ok(ApiResponse.error("Período de asistencia no encontrado")));
    }

    @GetMapping("/by-enrollment-semester")
    public ResponseEntity<ApiResponse<AttendancePeriodDTO>> getByEnrollmentAndSemester(
            @RequestParam String enrollmentId,
            @RequestParam String academicSemesterId) {
        Optional<AttendancePeriodDTO> period = attendancePeriodService
                .getByEnrollmentAndSemester(enrollmentId, academicSemesterId);
        return period.map(p -> ResponseEntity.ok(ApiResponse.success(p)))
                .orElseGet(() -> ResponseEntity.ok(ApiResponse.error("Período de asistencia no encontrado")));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<AttendancePeriodDTO>> createAttendancePeriod(
            @Valid @RequestBody CreateAttendancePeriodRequest request) {
        AttendancePeriodDTO period = attendancePeriodService.createAttendancePeriod(request);
        return ResponseEntity.ok(ApiResponse.success("Período de asistencia creado", period));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<AttendancePeriodDTO>> updateAttendancePeriod(
            @PathVariable String id,
            @Valid @RequestBody UpdateAttendancePeriodRequest request) {
        AttendancePeriodDTO period = attendancePeriodService.updateAttendancePeriod(id, request);
        return ResponseEntity.ok(ApiResponse.success("Período de asistencia actualizado", period));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteAttendancePeriod(@PathVariable String id) {
        attendancePeriodService.deleteAttendancePeriod(id);
        return ResponseEntity.ok(ApiResponse.success("Período de asistencia eliminado", null));
    }
}
