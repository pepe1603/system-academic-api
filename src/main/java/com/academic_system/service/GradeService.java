package com.academic_system.service;

import com.academic_system.dto.cpanel.CreateGradeRequest;
import com.academic_system.dto.cpanel.GradeDTO;
import com.academic_system.dto.cpanel.UpdateGradeRequest;
import com.academic_system.entity.postgres.*;
import com.academic_system.exception.BusinessRuleException;
import com.academic_system.exception.DuplicateResourceException;
import com.academic_system.exception.ResourceNotFoundException;
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
public class GradeService {

    private final GradeRepository gradeRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final EvaluationTypeRepository evaluationTypeRepository;
    private final StudentRepository studentRepository;
    private final CourseRepository courseRepository;

    @Transactional(readOnly = true)
    public Page<GradeDTO> getAllGrades(Pageable pageable) {
        Page<Grade> grades = gradeRepository.findAllByIsDeletedFalse(pageable);
        
        Set<UUID> enrollmentIds = grades.getContent().stream()
                .map(Grade::getEnrollmentId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        
        Set<UUID> evalTypeIds = grades.getContent().stream()
                .map(Grade::getEvaluationTypeId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        
        Map<UUID, Enrollment> enrollmentMap = enrollmentRepository.findAllById(enrollmentIds).stream()
                .collect(Collectors.toMap(Enrollment::getId, e -> e));
        
        Map<UUID, EvaluationType> evalTypeMap = evaluationTypeRepository.findAllById(evalTypeIds).stream()
                .collect(Collectors.toMap(EvaluationType::getId, et -> et));
        
        Set<UUID> studentIds = enrollmentMap.values().stream()
                .map(Enrollment::getStudentId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        
        Set<UUID> courseIds = enrollmentMap.values().stream()
                .map(Enrollment::getCourseId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        
        Map<UUID, Student> studentMap = studentRepository.findAllById(studentIds).stream()
                .collect(Collectors.toMap(Student::getId, s -> s));
        
        Map<UUID, Course> courseMap = courseRepository.findAllById(courseIds).stream()
                .collect(Collectors.toMap(Course::getId, c -> c));
        
        return grades.map(g -> toDTO(g, enrollmentMap, studentMap, courseMap, evalTypeMap));
    }

    @Transactional(readOnly = true)
    public Optional<GradeDTO> getGradeById(String id) {
        return gradeRepository.findById(UUID.fromString(id))
                .filter(g -> !Boolean.TRUE.equals(g.getIsDeleted()))
                .map(g -> toDTO(g, Collections.emptyMap(), Collections.emptyMap(), 
                               Collections.emptyMap(), Collections.emptyMap()));
    }

    @Transactional(readOnly = true)
    public List<GradeDTO> getGradesByEnrollment(String enrollmentId) {
        List<Grade> grades = gradeRepository.findByEnrollmentIdAndIsDeletedFalse(UUID.fromString(enrollmentId));
        
        Set<UUID> enrollmentIds = grades.stream()
                .map(Grade::getEnrollmentId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        
        Set<UUID> evalTypeIds = grades.stream()
                .map(Grade::getEvaluationTypeId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        
        Map<UUID, Enrollment> enrollmentMap = enrollmentRepository.findAllById(enrollmentIds).stream()
                .collect(Collectors.toMap(Enrollment::getId, e -> e));
        
        Map<UUID, EvaluationType> evalTypeMap = evaluationTypeRepository.findAllById(evalTypeIds).stream()
                .collect(Collectors.toMap(EvaluationType::getId, et -> et));
        
        Set<UUID> studentIds = enrollmentMap.values().stream()
                .map(Enrollment::getStudentId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        
        Set<UUID> courseIds = enrollmentMap.values().stream()
                .map(Enrollment::getCourseId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        
        Map<UUID, Student> studentMap = studentRepository.findAllById(studentIds).stream()
                .collect(Collectors.toMap(Student::getId, s -> s));
        
        Map<UUID, Course> courseMap = courseRepository.findAllById(courseIds).stream()
                .collect(Collectors.toMap(Course::getId, c -> c));
        
        return grades.stream()
                .map(g -> toDTO(g, enrollmentMap, studentMap, courseMap, evalTypeMap))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<GradeDTO> getDeletedGrades(Pageable pageable) {
        Page<Grade> grades = gradeRepository.findAllByIsDeletedTrue(pageable);
        
        Set<UUID> enrollmentIds = grades.getContent().stream()
                .map(Grade::getEnrollmentId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        
        Set<UUID> evalTypeIds = grades.getContent().stream()
                .map(Grade::getEvaluationTypeId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        
        Map<UUID, Enrollment> enrollmentMap = enrollmentRepository.findAllById(enrollmentIds).stream()
                .collect(Collectors.toMap(Enrollment::getId, e -> e));
        
        Map<UUID, EvaluationType> evalTypeMap = evaluationTypeRepository.findAllById(evalTypeIds).stream()
                .collect(Collectors.toMap(EvaluationType::getId, et -> et));
        
        Set<UUID> studentIds = enrollmentMap.values().stream()
                .map(Enrollment::getStudentId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        
        Set<UUID> courseIds = enrollmentMap.values().stream()
                .map(Enrollment::getCourseId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        
        Map<UUID, Student> studentMap = studentRepository.findAllById(studentIds).stream()
                .collect(Collectors.toMap(Student::getId, s -> s));
        
        Map<UUID, Course> courseMap = courseRepository.findAllById(courseIds).stream()
                .collect(Collectors.toMap(Course::getId, c -> c));
        
        return grades.getContent().stream()
                .map(g -> toDTO(g, enrollmentMap, studentMap, courseMap, evalTypeMap))
                .toList();
    }

    @Transactional
    public GradeDTO createGrade(CreateGradeRequest request) {
        Enrollment enrollment = enrollmentRepository.findById(request.getEnrollmentId())
                .orElseThrow(() -> new ResourceNotFoundException("Inscripción no encontrada", "Enrollment", "id"));

        EvaluationType evaluationType = evaluationTypeRepository.findById(request.getEvaluationTypeId())
                .orElseThrow(() -> new ResourceNotFoundException("Tipo de evaluación no encontrado", "EvaluationType", "id"));

        if (gradeRepository.existsByEnrollmentIdAndEvaluationTypeIdAndIsDeletedFalse(
                request.getEnrollmentId(), request.getEvaluationTypeId())) {
            throw new DuplicateResourceException("Ya existe una calificación para esta evaluación en la inscripción seleccionada", "Grade");
        }

        if (!evaluationType.getCourseId().equals(enrollment.getCourseId())) {
            throw new BusinessRuleException("El tipo de evaluación no pertenece al curso de la inscripción", "Grade", "evaluationTypeId");
        }

        Grade grade = Grade.builder()
                .enrollmentId(enrollment.getId())
                .evaluationTypeId(evaluationType.getId())
                .score(request.getScore())
                .build();

        grade = gradeRepository.save(grade);
        log.info("Created grade: {} for enrollment {}", grade.getScore(), enrollment.getId());
        return toDTO(grade, Collections.emptyMap(), Collections.emptyMap(), 
                     Collections.emptyMap(), Collections.emptyMap());
    }

    @Transactional
    public GradeDTO updateGrade(String id, UpdateGradeRequest request) {
        Grade grade = gradeRepository.findById(UUID.fromString(id))
                .orElseThrow(() -> new ResourceNotFoundException("Calificación no encontrada", "Grade", "id"));

        if (request.getScore() != null) grade.setScore(request.getScore());

        grade = gradeRepository.save(grade);
        log.info("Updated grade: {}", grade.getId());
        return toDTO(grade, Collections.emptyMap(), Collections.emptyMap(), 
                     Collections.emptyMap(), Collections.emptyMap());
    }

    @Transactional
    public void deleteGrade(String id) {
        Grade grade = gradeRepository.findById(UUID.fromString(id))
                .orElseThrow(() -> new ResourceNotFoundException("Calificación no encontrada", "Grade", "id"));
        grade.setIsDeleted(true);
        gradeRepository.save(grade);
        log.info("Deleted grade: {}", id);
    }

    private GradeDTO toDTO(Grade grade, Map<UUID, Enrollment> enrollmentMap, 
                           Map<UUID, Student> studentMap, Map<UUID, Course> courseMap,
                           Map<UUID, EvaluationType> evalTypeMap) {
        GradeDTO.GradeDTOBuilder builder = GradeDTO.builder()
                .id(grade.getId())
                .score(grade.getScore())
                .recordedAt(grade.getRecordedAt())
                .isDeleted(grade.getIsDeleted())
                .enrollmentId(grade.getEnrollmentId())
                .evaluationTypeId(grade.getEvaluationTypeId());

        if (grade.getEnrollmentId() != null) {
            Enrollment e = enrollmentMap.get(grade.getEnrollmentId());
            if (e == null && enrollmentMap.isEmpty()) {
                e = enrollmentRepository.findById(grade.getEnrollmentId()).orElse(null);
            }
            if (e != null) {
                builder.courseId(e.getCourseId());
                if (e.getStudentId() != null) {
                    Student s = studentMap.get(e.getStudentId());
                    if (s == null && studentMap.isEmpty()) {
                        s = studentRepository.findById(e.getStudentId()).orElse(null);
                    }
                    if (s != null) {
                        builder.studentName(s.getFirstName() + " " + s.getLastName());
                        builder.enrollmentNumber(s.getEnrollmentNumber());
                    }
                }
                if (e.getCourseId() != null) {
                    Course c = courseMap.get(e.getCourseId());
                    if (c == null && courseMap.isEmpty()) {
                        c = courseRepository.findById(e.getCourseId()).orElse(null);
                    }
                    if (c != null) {
                        builder.courseCode(c.getCourseCode());
                        builder.courseName(c.getName());
                    }
                }
            }
        }

        if (grade.getEvaluationTypeId() != null) {
            EvaluationType et = evalTypeMap.get(grade.getEvaluationTypeId());
            if (et == null && evalTypeMap.isEmpty()) {
                et = evaluationTypeRepository.findById(grade.getEvaluationTypeId()).orElse(null);
            }
            if (et != null) {
                builder.evaluationCode(et.getCode());
                builder.evaluationName(et.getName());
                builder.evaluationWeight(et.getWeight());
            }
        }

        return builder.build();
    }
}
