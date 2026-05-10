package com.academic_system.service;

import com.academic_system.dto.cpanel.AttendanceDTO;
import com.academic_system.dto.cpanel.CreateAttendanceRequest;
import com.academic_system.dto.cpanel.UpdateAttendanceRequest;
import com.academic_system.entity.postgres.Attendance;
import com.academic_system.entity.postgres.Enrollment;
import com.academic_system.repository.postgres.AttendanceRepository;
import com.academic_system.repository.postgres.CourseRepository;
import com.academic_system.repository.postgres.EnrollmentRepository;
import com.academic_system.repository.postgres.StudentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AttendanceService {

    private final AttendanceRepository attendanceRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final StudentRepository studentRepository;
    private final CourseRepository courseRepository;

    private static final List<String> VALID_STATUSES = List.of("PRESENT", "ABSENT", "JUSTIFIED", "LATE");

    @Transactional(readOnly = true)
    public Page<AttendanceDTO> getAllAttendances(Pageable pageable) {
        return attendanceRepository.findAllByIsDeletedFalse(pageable)
                .map(this::toDTO);
    }

    @Transactional(readOnly = true)
    public Optional<AttendanceDTO> getAttendanceById(String id) {
        return attendanceRepository.findById(UUID.fromString(id))
                .filter(a -> !Boolean.TRUE.equals(a.getIsDeleted()))
                .map(this::toDTO);
    }

    @Transactional(readOnly = true)
    public List<AttendanceDTO> getAttendancesByEnrollment(String enrollmentId) {
        return attendanceRepository.findByEnrollmentIdAndIsDeletedFalse(UUID.fromString(enrollmentId))
                .stream()
                .map(this::toDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<AttendanceDTO> getDeletedAttendances(Pageable pageable) {
        return attendanceRepository.findAllByIsDeletedTrue(pageable)
                .map(this::toDTO)
                .getContent();
    }

    @Transactional
    public AttendanceDTO createAttendance(CreateAttendanceRequest request) {
        Enrollment enrollment = enrollmentRepository.findById(request.getEnrollmentId())
                .orElseThrow(() -> new IllegalArgumentException("Inscripción no encontrada"));

        if (attendanceRepository.existsByEnrollmentIdAndAttendanceDateAndIsDeletedFalse(
                request.getEnrollmentId(), request.getAttendanceDate())) {
            throw new IllegalArgumentException("Ya existe un registro de asistencia para esta fecha");
        }

        String status = request.getStatus() != null ? request.getStatus().toUpperCase() : "PRESENT";
        if (!VALID_STATUSES.contains(status)) {
            throw new IllegalArgumentException("Estado inválido. Valores permitidos: PRESENT, ABSENT, JUSTIFIED, LATE");
        }

        Attendance attendance = Attendance.builder()
                .enrollmentId(enrollment.getId())
                .attendanceDate(request.getAttendanceDate())
                .status(status)
                .classTime(request.getClassTime())
                .subjectCode(request.getSubjectCode())
                .observations(request.getObservations())
                .build();

        attendance = attendanceRepository.save(attendance);
        log.info("Created attendance: {} for enrollment {}", attendance.getAttendanceDate(), enrollment.getId());
        return toDTO(attendance);
    }

    @Transactional
    public AttendanceDTO updateAttendance(String id, UpdateAttendanceRequest request) {
        Attendance attendance = attendanceRepository.findById(UUID.fromString(id))
                .orElseThrow(() -> new IllegalArgumentException("Registro de asistencia no encontrado"));

        if (request.getStatus() != null) {
            String newStatus = request.getStatus().toUpperCase();
            if (!VALID_STATUSES.contains(newStatus)) {
                throw new IllegalArgumentException("Estado inválido. Valores permitidos: PRESENT, ABSENT, JUSTIFIED, LATE");
            }
            attendance.setStatus(newStatus);
        }
        if (request.getClassTime() != null) attendance.setClassTime(request.getClassTime());
        if (request.getSubjectCode() != null) attendance.setSubjectCode(request.getSubjectCode());
        if (request.getObservations() != null) attendance.setObservations(request.getObservations());
        if (request.getJustifiedBy() != null) attendance.setJustifiedBy(request.getJustifiedBy());
        if (request.getJustificationDate() != null) attendance.setJustificationDate(request.getJustificationDate());

        attendance = attendanceRepository.save(attendance);
        log.info("Updated attendance: {}", attendance.getId());
        return toDTO(attendance);
    }

    @Transactional
    public void deleteAttendance(String id) {
        Attendance attendance = attendanceRepository.findById(UUID.fromString(id))
                .orElseThrow(() -> new IllegalArgumentException("Registro de asistencia no encontrado"));
        attendance.setIsDeleted(true);
        attendanceRepository.save(attendance);
        log.info("Deleted attendance: {}", id);
    }

    private AttendanceDTO toDTO(Attendance attendance) {
        AttendanceDTO.AttendanceDTOBuilder builder = AttendanceDTO.builder()
                .id(attendance.getId())
                .attendanceDate(attendance.getAttendanceDate())
                .status(attendance.getStatus())
                .classTime(attendance.getClassTime())
                .subjectCode(attendance.getSubjectCode())
                .observations(attendance.getObservations())
                .justificationDate(attendance.getJustificationDate())
                .recordedAt(attendance.getRecordedAt())
                .isDeleted(attendance.getIsDeleted())
                .enrollmentId(attendance.getEnrollmentId());

        if (attendance.getEnrollmentId() != null) {
            enrollmentRepository.findById(attendance.getEnrollmentId()).ifPresent(e -> {
                builder.courseId(e.getCourseId());
                if (e.getStudentId() != null) {
                    studentRepository.findById(e.getStudentId()).ifPresent(s -> {
                        builder.studentName(s.getFirstName() + " " + s.getLastName());
                        builder.enrollmentNumber(s.getEnrollmentNumber());
                    });
                }
                if (e.getCourseId() != null) {
                    courseRepository.findById(e.getCourseId()).ifPresent(c -> {
                        builder.courseCode(c.getCourseCode());
                        builder.courseName(c.getName());
                    });
                }
            });
        }

        return builder.build();
    }
}
