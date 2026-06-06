package com.academic_system.service;

import com.academic_system.dto.cpanel.*;
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

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExtraordinaryExamService {

    private final ExtraordinaryExamRepository extraordinaryExamRepository;
    private final StudentRepository studentRepository;
    private final CourseRepository courseRepository;
    private final AcademicSemesterRepository academicSemesterRepository;
    private final TeacherRepository teacherRepository;

    private static final List<String> VALID_STATUSES = List.of(
            "SCHEDULED", "APPLIED", "APPROVED", "FAILED", "CANCELLED", "NO_SHOW");

    @Transactional(readOnly = true)
    public Page<ExtraordinaryExamDTO> getAllExams(Pageable pageable) {
        return extraordinaryExamRepository.findAllByIsDeletedFalse(pageable)
                .map(this::toDTO);
    }

    @Transactional(readOnly = true)
    public Optional<ExtraordinaryExamDTO> getExamById(String id) {
        return extraordinaryExamRepository.findById(UUID.fromString(id))
                .filter(e -> !Boolean.TRUE.equals(e.getIsDeleted()))
                .map(this::toDTO);
    }

    @Transactional(readOnly = true)
    public List<ExtraordinaryExamDTO> getExamsByStudent(String studentId) {
        return extraordinaryExamRepository.findByStudentIdAndIsDeletedFalse(UUID.fromString(studentId))
                .stream()
                .map(this::toDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ExtraordinaryExamDTO> getExamsByCourse(String courseId) {
        return extraordinaryExamRepository.findByCourseIdAndIsDeletedFalse(UUID.fromString(courseId))
                .stream()
                .map(this::toDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ExtraordinaryExamDTO> getDeletedExams(Pageable pageable) {
        return extraordinaryExamRepository.findAllByIsDeletedTrue(pageable)
                .map(this::toDTO)
                .getContent();
    }

    @Transactional
    public ExtraordinaryExamDTO createExam(CreateExtraordinaryExamRequest request) {
        Student student = studentRepository.findById(request.getStudentId())
                .orElseThrow(() -> new ResourceNotFoundException("Estudiante no encontrado", "Student", "id"));

        Course course = courseRepository.findById(request.getCourseId())
                .orElseThrow(() -> new ResourceNotFoundException("Curso no encontrado", "Course", "id"));

        if (request.getAcademicSemesterId() != null) {
            academicSemesterRepository.findById(request.getAcademicSemesterId())
                    .orElseThrow(() -> new ResourceNotFoundException("Semestre académico no encontrado", "AcademicSemester", "id"));
        }

        int attempt = request.getAttemptNumber() != null ? request.getAttemptNumber() : 1;
        if (extraordinaryExamRepository.existsByStudentIdAndCourseIdAndAttemptNumberAndIsDeletedFalse(
                request.getStudentId(), request.getCourseId(), attempt)) {
            throw new DuplicateResourceException("Ya existe un examen extraordinario para este estudiante, curso e intento", "ExtraordinaryExam");
        }

        if (request.getExaminerId() != null) {
            teacherRepository.findById(request.getExaminerId())
                    .orElseThrow(() -> new ResourceNotFoundException("Docente examinador no encontrado", "Teacher", "id"));
        }

        ExtraordinaryExam exam = ExtraordinaryExam.builder()
                .studentId(student.getId())
                .courseId(course.getId())
                .academicSemesterId(request.getAcademicSemesterId())
                .attemptNumber(attempt)
                .scheduledDate(request.getScheduledDate())
                .applicationTime(request.getApplicationTime())
                .applicationLocation(request.getApplicationLocation())
                .previousGrade(request.getPreviousGrade())
                .examinerId(request.getExaminerId())
                .observation(request.getObservation())
                .cost(request.getCost() != null ? request.getCost() : BigDecimal.ZERO)
                .paymentReceipt(request.getPaymentReceipt())
                .paymentFolio(request.getPaymentFolio())
                .build();

        exam = extraordinaryExamRepository.save(exam);
        log.info("Created extraordinary exam: {} - {} (attempt {})",
                student.getEnrollmentNumber(), course.getCourseCode(), attempt);
        return toDTO(exam);
    }

    @Transactional
    public ExtraordinaryExamDTO updateExam(String id, UpdateExtraordinaryExamRequest request) {
        ExtraordinaryExam exam = extraordinaryExamRepository.findById(UUID.fromString(id))
                .orElseThrow(() -> new ResourceNotFoundException("Examen extraordinario no encontrado", "ExtraordinaryExam", "id"));

        if (request.getStatus() != null) {
            String newStatus = request.getStatus().toUpperCase();
            if (!VALID_STATUSES.contains(newStatus)) {
                throw new ValidationException("Estado inválido. Valores: SCHEDULED, APPLIED, APPROVED, FAILED, CANCELLED, NO_SHOW", "ExtraordinaryExam", "status");
            }
            exam.setStatus(newStatus);
        }
        if (request.getAttemptNumber() != null) exam.setAttemptNumber(request.getAttemptNumber());
        if (request.getScheduledDate() != null) exam.setScheduledDate(request.getScheduledDate());
        if (request.getApplicationDate() != null) exam.setApplicationDate(request.getApplicationDate());
        if (request.getApplicationTime() != null) exam.setApplicationTime(request.getApplicationTime());
        if (request.getApplicationLocation() != null) exam.setApplicationLocation(request.getApplicationLocation());
        if (request.getPreviousGrade() != null) exam.setPreviousGrade(request.getPreviousGrade());
        if (request.getGrade() != null) exam.setGrade(request.getGrade());
        if (request.getGradeLetter() != null) exam.setGradeLetter(request.getGradeLetter());
        if (request.getExaminerId() != null) {
            teacherRepository.findById(request.getExaminerId())
                    .orElseThrow(() -> new ResourceNotFoundException("Docente examinador no encontrado", "Teacher", "id"));
            exam.setExaminerId(request.getExaminerId());
        }
        if (request.getObservation() != null) exam.setObservation(request.getObservation());
        if (request.getCost() != null) exam.setCost(request.getCost());
        if (request.getPaymentReceipt() != null) exam.setPaymentReceipt(request.getPaymentReceipt());
        if (request.getPaymentFolio() != null) exam.setPaymentFolio(request.getPaymentFolio());

        exam = extraordinaryExamRepository.save(exam);
        log.info("Updated extraordinary exam: {}", exam.getId());
        return toDTO(exam);
    }

    @Transactional
    public void deleteExam(String id) {
        ExtraordinaryExam exam = extraordinaryExamRepository.findById(UUID.fromString(id))
                .orElseThrow(() -> new ResourceNotFoundException("Examen extraordinario no encontrado", "ExtraordinaryExam", "id"));
        exam.setIsDeleted(true);
        extraordinaryExamRepository.save(exam);
        log.info("Deleted extraordinary exam: {}", id);
    }

    private ExtraordinaryExamDTO toDTO(ExtraordinaryExam exam) {
        ExtraordinaryExamDTO.ExtraordinaryExamDTOBuilder builder = ExtraordinaryExamDTO.builder()
                .id(exam.getId())
                .attemptNumber(exam.getAttemptNumber())
                .status(exam.getStatus())
                .scheduledDate(exam.getScheduledDate())
                .applicationDate(exam.getApplicationDate())
                .applicationTime(exam.getApplicationTime())
                .applicationLocation(exam.getApplicationLocation())
                .previousGrade(exam.getPreviousGrade())
                .grade(exam.getGrade())
                .gradeLetter(exam.getGradeLetter())
                .observation(exam.getObservation())
                .cost(exam.getCost())
                .paymentReceipt(exam.getPaymentReceipt())
                .paymentFolio(exam.getPaymentFolio())
                .isDeleted(exam.getIsDeleted())
                .createdAt(exam.getCreatedAt())
                .studentId(exam.getStudentId())
                .courseId(exam.getCourseId())
                .academicSemesterId(exam.getAcademicSemesterId())
                .examinerId(exam.getExaminerId());

        if (exam.getStudentId() != null) {
            studentRepository.findById(exam.getStudentId()).ifPresent(s -> {
                builder.studentName(s.getFirstName() + " " + s.getLastName());
                builder.enrollmentNumber(s.getEnrollmentNumber());
            });
        }

        if (exam.getCourseId() != null) {
            courseRepository.findById(exam.getCourseId()).ifPresent(c -> {
                builder.courseCode(c.getCourseCode());
                builder.courseName(c.getName());
            });
        }

        if (exam.getAcademicSemesterId() != null) {
            academicSemesterRepository.findById(exam.getAcademicSemesterId()).ifPresent(as ->
                    builder.academicSemesterName(as.getName()));
        }

        if (exam.getExaminerId() != null) {
            teacherRepository.findById(exam.getExaminerId()).ifPresent(t ->
                    builder.examinerName(t.getFirstName() + " " + t.getLastName()));
        }

        return builder.build();
    }
}
