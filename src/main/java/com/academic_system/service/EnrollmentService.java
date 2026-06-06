package com.academic_system.service;

import com.academic_system.dto.cpanel.CreateEnrollmentRequest;
import com.academic_system.dto.cpanel.EnrollmentDTO;
import com.academic_system.dto.cpanel.UpdateEnrollmentRequest;
import com.academic_system.entity.postgres.*;
import com.academic_system.exception.DuplicateResourceException;
import com.academic_system.exception.ResourceNotFoundException;
import com.academic_system.exception.ValidationException;
import com.academic_system.repository.postgres.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

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
        Page<Enrollment> enrollments = enrollmentRepository.findAllByIsDeletedFalse(pageable);
        
        Set<UUID> studentIds = enrollments.getContent().stream()
                .map(Enrollment::getStudentId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        
        Set<UUID> courseIds = enrollments.getContent().stream()
                .map(Enrollment::getCourseId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        
        Set<UUID> periodIds = enrollments.getContent().stream()
                .map(Enrollment::getAcademicPeriodId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        
        Set<UUID> groupIds = enrollments.getContent().stream()
                .map(Enrollment::getGroupId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        
        Map<UUID, Student> studentMap = studentRepository.findAllById(studentIds).stream()
                .collect(Collectors.toMap(Student::getId, s -> s));
        
        Map<UUID, Course> courseMap = courseRepository.findAllById(courseIds).stream()
                .collect(Collectors.toMap(Course::getId, c -> c));
        
        Map<UUID, AcademicPeriod> periodMap = academicPeriodRepository.findAllById(periodIds).stream()
                .collect(Collectors.toMap(AcademicPeriod::getId, p -> p));
        
        Map<UUID, AcademicGroup> groupMap = academicGroupRepository.findAllById(groupIds).stream()
                .collect(Collectors.toMap(AcademicGroup::getId, g -> g));
        
        return enrollments.map(e -> toDTO(e, studentMap, courseMap, periodMap, groupMap));
    }

    @Transactional(readOnly = true)
    public Optional<EnrollmentDTO> getEnrollmentById(String id) {
        return enrollmentRepository.findById(UUID.fromString(id))
                .filter(e -> !Boolean.TRUE.equals(e.getIsDeleted()))
                .map(e -> toDTO(e, Collections.emptyMap(), Collections.emptyMap(), 
                               Collections.emptyMap(), Collections.emptyMap()));
    }

    @Transactional(readOnly = true)
    public List<EnrollmentDTO> getDeletedEnrollments(Pageable pageable) {
        Page<Enrollment> enrollments = enrollmentRepository.findAllByIsDeletedTrue(pageable);
        
        Set<UUID> studentIds = enrollments.getContent().stream()
                .map(Enrollment::getStudentId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        
        Set<UUID> courseIds = enrollments.getContent().stream()
                .map(Enrollment::getCourseId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        
        Set<UUID> periodIds = enrollments.getContent().stream()
                .map(Enrollment::getAcademicPeriodId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        
        Set<UUID> groupIds = enrollments.getContent().stream()
                .map(Enrollment::getGroupId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        
        Map<UUID, Student> studentMap = studentRepository.findAllById(studentIds).stream()
                .collect(Collectors.toMap(Student::getId, s -> s));
        
        Map<UUID, Course> courseMap = courseRepository.findAllById(courseIds).stream()
                .collect(Collectors.toMap(Course::getId, c -> c));
        
        Map<UUID, AcademicPeriod> periodMap = academicPeriodRepository.findAllById(periodIds).stream()
                .collect(Collectors.toMap(AcademicPeriod::getId, p -> p));
        
        Map<UUID, AcademicGroup> groupMap = academicGroupRepository.findAllById(groupIds).stream()
                .collect(Collectors.toMap(AcademicGroup::getId, g -> g));
        
        return enrollments.getContent().stream()
                .map(e -> toDTO(e, studentMap, courseMap, periodMap, groupMap))
                .toList();
    }

    @Transactional
    public EnrollmentDTO createEnrollment(CreateEnrollmentRequest request) {
        if (enrollmentRepository.existsByStudentIdAndCourseIdAndAcademicPeriodIdAndIsDeletedFalse(
                request.getStudentId(), request.getCourseId(), request.getAcademicPeriodId())) {
            throw new DuplicateResourceException("El estudiante ya está inscrito en este curso para el período seleccionado", "Enrollment");
        }

        Student student = studentRepository.findById(request.getStudentId())
                .orElseThrow(() -> new ResourceNotFoundException("Estudiante no encontrado", "Student", "id"));

        Course course = courseRepository.findById(request.getCourseId())
                .orElseThrow(() -> new ResourceNotFoundException("Curso no encontrado", "Course", "id"));

        AcademicPeriod academicPeriod = academicPeriodRepository.findById(request.getAcademicPeriodId())
                .orElseThrow(() -> new ResourceNotFoundException("Período académico no encontrado", "AcademicPeriod", "id"));

        if (request.getGroupId() != null) {
            academicGroupRepository.findById(request.getGroupId())
                    .orElseThrow(() -> new ResourceNotFoundException("Grupo académico no encontrado", "AcademicGroup", "id"));
        }

        String status = request.getStatus() != null ? request.getStatus().toUpperCase() : "ENROLLED";
        if (!List.of("ENROLLED", "APPROVED", "FAILED", "WITHDRAWN").contains(status)) {
            throw new ValidationException("Estado inválido. Valores permitidos: ENROLLED, APPROVED, FAILED, WITHDRAWN", "Enrollment", "status");
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
        return toDTO(enrollment, Collections.emptyMap(), Collections.emptyMap(), 
                     Collections.emptyMap(), Collections.emptyMap());
    }

    @Transactional
    public EnrollmentDTO updateEnrollment(String id, UpdateEnrollmentRequest request) {
        Enrollment enrollment = enrollmentRepository.findById(UUID.fromString(id))
                .orElseThrow(() -> new ResourceNotFoundException("Inscripción no encontrada", "Enrollment", "id"));

        if (request.getStatus() != null) {
            String newStatus = request.getStatus().toUpperCase();
            if (!List.of("ENROLLED", "APPROVED", "FAILED", "WITHDRAWN").contains(newStatus)) {
                throw new ValidationException("Estado inválido. Valores permitidos: ENROLLED, APPROVED, FAILED, WITHDRAWN", "Enrollment", "status");
            }
            enrollment.setStatus(newStatus);
        }
        if (request.getGroupId() != null) {
            academicGroupRepository.findById(request.getGroupId())
                    .orElseThrow(() -> new ResourceNotFoundException("Grupo académico no encontrado", "AcademicGroup", "id"));
            enrollment.setGroupId(request.getGroupId());
        }
        if (request.getIsActive() != null) enrollment.setIsActive(request.getIsActive());

        enrollment = enrollmentRepository.save(enrollment);
        log.info("Updated enrollment: {}", enrollment.getId());
        return toDTO(enrollment, Collections.emptyMap(), Collections.emptyMap(), 
                     Collections.emptyMap(), Collections.emptyMap());
    }

    @Transactional
    public void deleteEnrollment(String id) {
        Enrollment enrollment = enrollmentRepository.findById(UUID.fromString(id))
                .orElseThrow(() -> new ResourceNotFoundException("Inscripción no encontrada", "Enrollment", "id"));
        enrollment.setIsDeleted(true);
        enrollmentRepository.save(enrollment);
        log.info("Deleted enrollment: {}", id);
    }

    private EnrollmentDTO toDTO(Enrollment enrollment, Map<UUID, Student> studentMap, 
                                 Map<UUID, Course> courseMap, Map<UUID, AcademicPeriod> periodMap,
                                 Map<UUID, AcademicGroup> groupMap) {
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
            Student s = studentMap.get(enrollment.getStudentId());
            if (s == null && studentMap.isEmpty()) {
                s = studentRepository.findById(enrollment.getStudentId()).orElse(null);
            }
            if (s != null) {
                builder.studentName(s.getFirstName() + " " + s.getLastName());
                builder.enrollmentNumber(s.getEnrollmentNumber());
            }
        }

        if (enrollment.getCourseId() != null) {
            Course c = courseMap.get(enrollment.getCourseId());
            if (c == null && courseMap.isEmpty()) {
                c = courseRepository.findById(enrollment.getCourseId()).orElse(null);
            }
            if (c != null) {
                builder.courseCode(c.getCourseCode());
                builder.courseName(c.getName());
            }
        }

        if (enrollment.getAcademicPeriodId() != null) {
            AcademicPeriod ap = periodMap.get(enrollment.getAcademicPeriodId());
            if (ap == null && periodMap.isEmpty()) {
                ap = academicPeriodRepository.findById(enrollment.getAcademicPeriodId()).orElse(null);
            }
            if (ap != null) {
                builder.academicPeriodName(ap.getName());
            }
        }

        if (enrollment.getGroupId() != null) {
            AcademicGroup g = groupMap.get(enrollment.getGroupId());
            if (g == null && groupMap.isEmpty()) {
                g = academicGroupRepository.findById(enrollment.getGroupId()).orElse(null);
            }
            if (g != null) {
                builder.groupName(g.getName());
            }
        }

        return builder.build();
    }
}
