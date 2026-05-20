package com.academic_system.service;

import com.academic_system.dto.cpanel.AttendancePeriodDTO;
import com.academic_system.dto.cpanel.CreateAttendancePeriodRequest;
import com.academic_system.dto.cpanel.UpdateAttendancePeriodRequest;
import com.academic_system.entity.postgres.AttendancePeriod;
import com.academic_system.repository.postgres.AttendancePeriodRepository;
import com.academic_system.repository.postgres.CourseRepository;
import com.academic_system.repository.postgres.EnrollmentRepository;
import com.academic_system.repository.postgres.StudentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AttendancePeriodService {

    private final AttendancePeriodRepository attendancePeriodRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final StudentRepository studentRepository;
    private final CourseRepository courseRepository;

    @Transactional(readOnly = true)
    public Page<AttendancePeriodDTO> getAllAttendancePeriods(Pageable pageable) {
        return attendancePeriodRepository.findAll(pageable)
                .map(this::toDTO);
    }

    @Transactional(readOnly = true)
    public Optional<AttendancePeriodDTO> getAttendancePeriodById(String id) {
        return attendancePeriodRepository.findById(UUID.fromString(id))
                .map(this::toDTO);
    }

    @Transactional(readOnly = true)
    public Optional<AttendancePeriodDTO> getByEnrollmentAndSemester(String enrollmentId, String semesterId) {
        return attendancePeriodRepository
                .findByEnrollmentIdAndAcademicSemesterId(UUID.fromString(enrollmentId), UUID.fromString(semesterId))
                .map(this::toDTO);
    }

    @Transactional(readOnly = true)
    public Page<AttendancePeriodDTO> getByEnrollment(String enrollmentId, Pageable pageable) {
        return attendancePeriodRepository
                .findByEnrollmentIdOrderByCreatedAtDesc(UUID.fromString(enrollmentId), pageable)
                .map(this::toDTO);
    }

    @Transactional(readOnly = true)
    public Page<AttendancePeriodDTO> getByAcademicSemester(String semesterId, Pageable pageable) {
        return attendancePeriodRepository
                .findByAcademicSemesterIdOrderByCreatedAtDesc(UUID.fromString(semesterId), pageable)
                .map(this::toDTO);
    }

    @Transactional
    public AttendancePeriodDTO createAttendancePeriod(CreateAttendancePeriodRequest request) {
        if (!enrollmentRepository.existsById(request.getEnrollmentId())) {
            throw new IllegalArgumentException("Inscripción no encontrada");
        }

        if (attendancePeriodRepository.existsByEnrollmentIdAndAcademicSemesterId(
                request.getEnrollmentId(), request.getAcademicSemesterId())) {
            throw new IllegalArgumentException("Ya existe un período de asistencia para esta inscripción y semestre");
        }

        AttendancePeriod period = AttendancePeriod.builder()
                .enrollmentId(request.getEnrollmentId())
                .academicSemesterId(request.getAcademicSemesterId())
                .totalClasses(request.getTotalClasses() != null ? request.getTotalClasses() : 0)
                .totalPresent(request.getTotalPresent() != null ? request.getTotalPresent() : 0)
                .totalAbsent(request.getTotalAbsent() != null ? request.getTotalAbsent() : 0)
                .totalJustified(request.getTotalJustified() != null ? request.getTotalJustified() : 0)
                .totalLate(request.getTotalLate() != null ? request.getTotalLate() : 0)
                .observations(request.getObservations())
                .build();

        period = attendancePeriodRepository.save(period);
        log.info("Created attendance period: {} for enrollment {}", period.getId(), period.getEnrollmentId());
        return toDTO(period);
    }

    @Transactional
    public AttendancePeriodDTO updateAttendancePeriod(String id, UpdateAttendancePeriodRequest request) {
        AttendancePeriod period = attendancePeriodRepository.findById(UUID.fromString(id))
                .orElseThrow(() -> new IllegalArgumentException("Período de asistencia no encontrado"));

        if (request.getTotalClasses() != null) period.setTotalClasses(request.getTotalClasses());
        if (request.getTotalPresent() != null) period.setTotalPresent(request.getTotalPresent());
        if (request.getTotalAbsent() != null) period.setTotalAbsent(request.getTotalAbsent());
        if (request.getTotalJustified() != null) period.setTotalJustified(request.getTotalJustified());
        if (request.getTotalLate() != null) period.setTotalLate(request.getTotalLate());
        if (request.getObservations() != null) period.setObservations(request.getObservations());

        period = attendancePeriodRepository.save(period);
        log.info("Updated attendance period: {}", period.getId());
        return toDTO(period);
    }

    @Transactional
    public void deleteAttendancePeriod(String id) {
        AttendancePeriod period = attendancePeriodRepository.findById(UUID.fromString(id))
                .orElseThrow(() -> new IllegalArgumentException("Período de asistencia no encontrado"));
        attendancePeriodRepository.delete(period);
        log.info("Deleted attendance period: {}", id);
    }

    private AttendancePeriodDTO toDTO(AttendancePeriod period) {
        AttendancePeriodDTO.AttendancePeriodDTOBuilder builder = AttendancePeriodDTO.builder()
                .id(period.getId())
                .enrollmentId(period.getEnrollmentId())
                .academicSemesterId(period.getAcademicSemesterId())
                .totalClasses(period.getTotalClasses())
                .totalPresent(period.getTotalPresent())
                .totalAbsent(period.getTotalAbsent())
                .totalJustified(period.getTotalJustified())
                .totalLate(period.getTotalLate())
                .attendancePercentage(period.getAttendancePercentage())
                .attendanceStatus(period.getAttendanceStatus())
                .observations(period.getObservations())
                .createdAt(period.getCreatedAt())
                .updatedAt(period.getUpdatedAt());

        if (period.getEnrollmentId() != null) {
            enrollmentRepository.findById(period.getEnrollmentId()).ifPresent(e -> {
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
