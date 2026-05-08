package com.academic_system.service;

import com.academic_system.dto.cpanel.CreateEnrollmentRequest;
import com.academic_system.dto.cpanel.EnrollmentDTO;
import com.academic_system.dto.cpanel.UpdateEnrollmentRequest;
import com.academic_system.entity.postgres.*;
import com.academic_system.repository.postgres.*;
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
public class EnrollmentService {

    private final EnrollmentRepository enrollmentRepository;
    private final StudentRepository studentRepository;
    private final CourseRepository courseRepository;
    private final AcademicPeriodRepository academicPeriodRepository;
    private final AcademicGroupRepository academicGroupRepository;

    @Transactional(readOnly = true)
    public Page<EnrollmentDTO> getAllEnrollments(Pageable pageable) {
        return enrollmentRepository.findAllByIsDeletedFalse(pageable)
                .map(this::toDTO);
    }

    @Transactional(readOnly = true)
    public Optional<EnrollmentDTO> getEnrollmentById(String id) {
        return enrollmentRepository.findById(UUID.fromString(id))
                .filter(e -> !Boolean.TRUE.equals(e.getIsDeleted()))
                .map(this::toDTO);
    }

    @Transactional(readOnly = true)
    public List<EnrollmentDTO> getDeletedEnrollments(Pageable pageable) {
        return enrollmentRepository.findAllByIsDeletedTrue(pageable)
                .map(this::toDTO)
                .getContent();
    }

    @Transactional
    public EnrollmentDTO createEnrollment(CreateEnrollmentRequest request) {
        if (enrollmentRepository.existsByStudentIdAndCourseIdAndAcademicPeriodIdAndIsDeletedFalse(
                request.getStudentId(), request.getCourseId(), request.getAcademicPeriodId())) {
            throw new IllegalArgumentException("El estudiante ya está inscrito en este curso para el período seleccionado");
        }

        Student student = studentRepository.findById(request.getStudentId())
                .orElseThrow(() -> new IllegalArgumentException("Estudiante no encontrado"));

        Course course = courseRepository.findById(request.getCourseId())
                .orElseThrow(() -> new IllegalArgumentException("Curso no encontrado"));

        AcademicPeriod academicPeriod = academicPeriodRepository.findById(request.getAcademicPeriodId())
                .orElseThrow(() -> new IllegalArgumentException("Período académico no encontrado"));

        if (request.getGroupId() != null) {
            academicGroupRepository.findById(request.getGroupId())
                    .orElseThrow(() -> new IllegalArgumentException("Grupo académico no encontrado"));
        }

        String status = request.getStatus() != null ? request.getStatus().toUpperCase() : "ENROLLED";
        if (!List.of("ENROLLED", "APPROVED", "FAILED", "WITHDRAWN").contains(status)) {
            throw new IllegalArgumentException("Estado inválido. Valores permitidos: ENROLLED, APPROVED, FAILED, WITHDRAWN");
        }

        Enrollment enrollment = Enrollment.builder()
                .studentId(student.getId())
                .courseId(course.getId())
                .academicPeriodId(academicPeriod.getId())
                .groupId(request.getGroupId())
                .status(status)
                .build();

        enrollment = enrollmentRepository.save(enrollment);
        log.info("Created enrollment: {} - {} ({})", student.getEnrollmentNumber(), course.getCourseCode(), enrollment.getId());
        return toDTO(enrollment);
    }

    @Transactional
    public EnrollmentDTO updateEnrollment(String id, UpdateEnrollmentRequest request) {
        Enrollment enrollment = enrollmentRepository.findById(UUID.fromString(id))
                .orElseThrow(() -> new IllegalArgumentException("Inscripción no encontrada"));

        if (request.getStatus() != null) {
            String newStatus = request.getStatus().toUpperCase();
            if (!List.of("ENROLLED", "APPROVED", "FAILED", "WITHDRAWN").contains(newStatus)) {
                throw new IllegalArgumentException("Estado inválido. Valores permitidos: ENROLLED, APPROVED, FAILED, WITHDRAWN");
            }
            enrollment.setStatus(newStatus);
        }
        if (request.getGroupId() != null) {
            academicGroupRepository.findById(request.getGroupId())
                    .orElseThrow(() -> new IllegalArgumentException("Grupo académico no encontrado"));
            enrollment.setGroupId(request.getGroupId());
        }
        if (request.getIsActive() != null) enrollment.setIsActive(request.getIsActive());

        enrollment = enrollmentRepository.save(enrollment);
        log.info("Updated enrollment: {}", enrollment.getId());
        return toDTO(enrollment);
    }

    @Transactional
    public void deleteEnrollment(String id) {
        Enrollment enrollment = enrollmentRepository.findById(UUID.fromString(id))
                .orElseThrow(() -> new IllegalArgumentException("Inscripción no encontrada"));
        enrollment.setIsDeleted(true);
        enrollmentRepository.save(enrollment);
        log.info("Deleted enrollment: {}", id);
    }

    private EnrollmentDTO toDTO(Enrollment enrollment) {
        EnrollmentDTO.EnrollmentDTOBuilder builder = EnrollmentDTO.builder()
                .id(enrollment.getId())
                .status(enrollment.getStatus())
                .isActive(enrollment.getIsActive())
                .isDeleted(enrollment.getIsDeleted())
                .createdAt(enrollment.getCreatedAt())
                .studentId(enrollment.getStudentId())
                .courseId(enrollment.getCourseId())
                .academicPeriodId(enrollment.getAcademicPeriodId())
                .groupId(enrollment.getGroupId());

        if (enrollment.getStudentId() != null) {
            studentRepository.findById(enrollment.getStudentId()).ifPresent(s -> {
                builder.studentName(s.getFirstName() + " " + s.getLastName());
                builder.enrollmentNumber(s.getEnrollmentNumber());
            });
        }

        if (enrollment.getCourseId() != null) {
            courseRepository.findById(enrollment.getCourseId()).ifPresent(c -> {
                builder.courseCode(c.getCourseCode());
                builder.courseName(c.getName());
            });
        }

        if (enrollment.getAcademicPeriodId() != null) {
            academicPeriodRepository.findById(enrollment.getAcademicPeriodId()).ifPresent(ap ->
                    builder.academicPeriodName(ap.getName()));
        }

        if (enrollment.getGroupId() != null) {
            academicGroupRepository.findById(enrollment.getGroupId()).ifPresent(g ->
                    builder.groupName(g.getName()));
        }

        return builder.build();
    }
}
