package com.academic_system.service;

import com.academic_system.dto.cpanel.CreateEvaluationTypeRequest;
import com.academic_system.dto.cpanel.EvaluationTypeDTO;
import com.academic_system.dto.cpanel.UpdateEvaluationTypeRequest;
import com.academic_system.entity.postgres.Course;
import com.academic_system.entity.postgres.EvaluationType;
import com.academic_system.exception.DuplicateResourceException;
import com.academic_system.exception.ResourceNotFoundException;
import com.academic_system.repository.postgres.CourseRepository;
import com.academic_system.repository.postgres.EvaluationTypeRepository;
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
public class EvaluationTypeService {

    private final EvaluationTypeRepository evaluationTypeRepository;
    private final CourseRepository courseRepository;

    @Transactional(readOnly = true)
    public Page<EvaluationTypeDTO> getAllEvaluationTypes(Pageable pageable) {
        return evaluationTypeRepository.findAllByIsActiveTrue(pageable)
                .map(this::toDTO);
    }

    @Transactional(readOnly = true)
    public Optional<EvaluationTypeDTO> getEvaluationTypeById(String id) {
        return evaluationTypeRepository.findById(UUID.fromString(id))
                .filter(et -> Boolean.TRUE.equals(et.getIsActive()))
                .map(this::toDTO);
    }

    @Transactional(readOnly = true)
    public List<EvaluationTypeDTO> getInactiveEvaluationTypes(Pageable pageable) {
        return evaluationTypeRepository.findAllByIsActiveFalse(pageable)
                .map(this::toDTO)
                .getContent();
    }

    @Transactional(readOnly = true)
    public List<EvaluationTypeDTO> getEvaluationTypesByCourse(String courseId) {
        return evaluationTypeRepository.findByCourseIdAndIsActiveTrue(UUID.fromString(courseId))
                .stream()
                .map(this::toDTO)
                .toList();
    }

    @Transactional
    public EvaluationTypeDTO createEvaluationType(CreateEvaluationTypeRequest request) {
        Course course = courseRepository.findById(request.getCourseId())
                .orElseThrow(() -> new ResourceNotFoundException("Curso no encontrado", "Course", "id"));

        if (evaluationTypeRepository.existsByCourseIdAndCodeAndIsActiveTrue(
                request.getCourseId(), request.getCode().toUpperCase())) {
            throw new DuplicateResourceException("Ya existe un tipo de evaluación con ese código en el curso", "EvaluationType", "code");
        }

        EvaluationType evaluationType = EvaluationType.builder()
                .courseId(course.getId())
                .code(request.getCode().toUpperCase())
                .name(request.getName())
                .weight(request.getWeight())
                .build();

        evaluationType = evaluationTypeRepository.save(evaluationType);
        log.info("Created evaluation type: {} for course {}", evaluationType.getCode(), course.getCourseCode());
        return toDTO(evaluationType);
    }

    @Transactional
    public EvaluationTypeDTO updateEvaluationType(String id, UpdateEvaluationTypeRequest request) {
        EvaluationType evaluationType = evaluationTypeRepository.findById(UUID.fromString(id))
                .orElseThrow(() -> new ResourceNotFoundException("Tipo de evaluación no encontrado", "EvaluationType", "id"));

        if (request.getCourseId() != null) {
            courseRepository.findById(request.getCourseId())
                    .orElseThrow(() -> new ResourceNotFoundException("Curso no encontrado", "Course", "id"));
            evaluationType.setCourseId(request.getCourseId());
        }
        if (request.getCode() != null) {
            String newCode = request.getCode().toUpperCase();
            UUID effectiveCourseId = request.getCourseId() != null ? request.getCourseId() : evaluationType.getCourseId();
            if (!evaluationType.getCode().equals(newCode) &&
                    evaluationTypeRepository.existsByCourseIdAndCodeAndIsActiveTrueAndIdNot(effectiveCourseId, newCode, evaluationType.getId())) {
                throw new DuplicateResourceException("Ya existe un tipo de evaluación con ese código en el curso", "EvaluationType", "code");
            }
            evaluationType.setCode(newCode);
        }
        if (request.getName() != null) evaluationType.setName(request.getName());
        if (request.getWeight() != null) evaluationType.setWeight(request.getWeight());
        if (request.getIsActive() != null) evaluationType.setIsActive(request.getIsActive());

        evaluationType = evaluationTypeRepository.save(evaluationType);
        log.info("Updated evaluation type: {}", evaluationType.getId());
        return toDTO(evaluationType);
    }

    @Transactional
    public void deleteEvaluationType(String id) {
        EvaluationType evaluationType = evaluationTypeRepository.findById(UUID.fromString(id))
                .orElseThrow(() -> new ResourceNotFoundException("Tipo de evaluación no encontrado", "EvaluationType", "id"));
        evaluationTypeRepository.delete(evaluationType);
        log.info("Deleted evaluation type: {}", id);
    }

    private EvaluationTypeDTO toDTO(EvaluationType evaluationType) {
        EvaluationTypeDTO.EvaluationTypeDTOBuilder builder = EvaluationTypeDTO.builder()
                .id(evaluationType.getId())
                .code(evaluationType.getCode())
                .name(evaluationType.getName())
                .weight(evaluationType.getWeight())
                .isActive(evaluationType.getIsActive())
                .createdAt(evaluationType.getCreatedAt())
                .courseId(evaluationType.getCourseId());

        if (evaluationType.getCourseId() != null) {
            courseRepository.findById(evaluationType.getCourseId()).ifPresent(c -> {
                builder.courseCode(c.getCourseCode());
                builder.courseName(c.getName());
            });
        }

        return builder.build();
    }
}
