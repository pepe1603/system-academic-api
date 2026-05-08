package com.academic_system.service;

import com.academic_system.dto.cpanel.CreateGradeRequest;
import com.academic_system.dto.cpanel.GradeDTO;
import com.academic_system.dto.cpanel.UpdateGradeRequest;
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
public class GradeService {

    private final GradeRepository gradeRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final EvaluationTypeRepository evaluationTypeRepository;
    private final StudentRepository studentRepository;
    private final CourseRepository courseRepository;

    @Transactional(readOnly = true)
    public Page<GradeDTO> getAllGrades(Pageable pageable) {
        return gradeRepository.findAllByIsDeletedFalse(pageable)
                .map(this::toDTO);
    }

    @Transactional(readOnly = true)
    public Optional<GradeDTO> getGradeById(String id) {
        return gradeRepository.findById(UUID.fromString(id))
                .filter(g -> !Boolean.TRUE.equals(g.getIsDeleted()))
                .map(this::toDTO);
    }

    @Transactional(readOnly = true)
    public List<GradeDTO> getGradesByEnrollment(String enrollmentId) {
        return gradeRepository.findByEnrollmentIdAndIsDeletedFalse(UUID.fromString(enrollmentId))
                .stream()
                .map(this::toDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<GradeDTO> getDeletedGrades(Pageable pageable) {
        return gradeRepository.findAllByIsDeletedTrue(pageable)
                .map(this::toDTO)
                .getContent();
    }

    @Transactional
    public GradeDTO createGrade(CreateGradeRequest request) {
        Enrollment enrollment = enrollmentRepository.findById(request.getEnrollmentId())
                .orElseThrow(() -> new IllegalArgumentException("Inscripción no encontrada"));

        EvaluationType evaluationType = evaluationTypeRepository.findById(request.getEvaluationTypeId())
                .orElseThrow(() -> new IllegalArgumentException("Tipo de evaluación no encontrado"));

        if (gradeRepository.existsByEnrollmentIdAndEvaluationTypeIdAndIsDeletedFalse(
                request.getEnrollmentId(), request.getEvaluationTypeId())) {
            throw new IllegalArgumentException("Ya existe una calificación para esta evaluación en la inscripción seleccionada");
        }

        if (!evaluationType.getCourseId().equals(enrollment.getCourseId())) {
            throw new IllegalArgumentException("El tipo de evaluación no pertenece al curso de la inscripción");
        }

        Grade grade = Grade.builder()
                .enrollmentId(enrollment.getId())
                .evaluationTypeId(evaluationType.getId())
                .score(request.getScore())
                .build();

        grade = gradeRepository.save(grade);
        log.info("Created grade: {} for enrollment {}", grade.getScore(), enrollment.getId());
        return toDTO(grade);
    }

    @Transactional
    public GradeDTO updateGrade(String id, UpdateGradeRequest request) {
        Grade grade = gradeRepository.findById(UUID.fromString(id))
                .orElseThrow(() -> new IllegalArgumentException("Calificación no encontrada"));

        if (request.getScore() != null) grade.setScore(request.getScore());

        grade = gradeRepository.save(grade);
        log.info("Updated grade: {}", grade.getId());
        return toDTO(grade);
    }

    @Transactional
    public void deleteGrade(String id) {
        Grade grade = gradeRepository.findById(UUID.fromString(id))
                .orElseThrow(() -> new IllegalArgumentException("Calificación no encontrada"));
        grade.setIsDeleted(true);
        gradeRepository.save(grade);
        log.info("Deleted grade: {}", id);
    }

    private GradeDTO toDTO(Grade grade) {
        GradeDTO.GradeDTOBuilder builder = GradeDTO.builder()
                .id(grade.getId())
                .score(grade.getScore())
                .recordedAt(grade.getRecordedAt())
                .isDeleted(grade.getIsDeleted())
                .enrollmentId(grade.getEnrollmentId())
                .evaluationTypeId(grade.getEvaluationTypeId());

        if (grade.getEnrollmentId() != null) {
            enrollmentRepository.findById(grade.getEnrollmentId()).ifPresent(e -> {
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

        if (grade.getEvaluationTypeId() != null) {
            evaluationTypeRepository.findById(grade.getEvaluationTypeId()).ifPresent(et -> {
                builder.evaluationCode(et.getCode());
                builder.evaluationName(et.getName());
                builder.evaluationWeight(et.getWeight());
            });
        }

        return builder.build();
    }
}
