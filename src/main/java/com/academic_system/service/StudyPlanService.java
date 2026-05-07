package com.academic_system.service;

import com.academic_system.dto.cpanel.CreateStudyPlanRequest;
import com.academic_system.dto.cpanel.StudyPlanDTO;
import com.academic_system.dto.cpanel.UpdateStudyPlanRequest;
import com.academic_system.entity.postgres.StudyPlan;
import com.academic_system.repository.postgres.StudyPlanRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class StudyPlanService {

    private final StudyPlanRepository studyPlanRepository;

    @Transactional(readOnly = true)
    public Page<StudyPlanDTO> getAllStudyPlans(Pageable pageable) {
        return studyPlanRepository.findAllByIsDeletedFalse(pageable)
                .map(this::toDTO);
    }

    @Transactional(readOnly = true)
    public Optional<StudyPlanDTO> getStudyPlanById(String id) {
        return studyPlanRepository.findById(java.util.UUID.fromString(id))
                .filter(sp -> !Boolean.TRUE.equals(sp.getIsDeleted()))
                .map(this::toDTO);
    }

    @Transactional(readOnly = true)
    public List<StudyPlanDTO> getDeletedStudyPlans(Pageable pageable) {
        return studyPlanRepository.findAllByIsDeletedTrue(pageable)
                .map(this::toDTO)
                .getContent();
    }

    @Transactional
    public StudyPlanDTO createStudyPlan(CreateStudyPlanRequest request) {
        if (studyPlanRepository.existsByCodeAndIsDeletedFalse(request.getCode())) {
            throw new IllegalArgumentException("Ya existe un plan de estudio con ese código");
        }

        StudyPlan studyPlan = StudyPlan.builder()
                .code(request.getCode().toUpperCase())
                .name(request.getName())
                .version(request.getVersion())
                .description(request.getDescription())
                .titleDegree(request.getTitleDegree())
                .totalCredits(request.getTotalCredits())
                .durationSemesters(request.getDurationSemesters())
                .build();

        studyPlan = studyPlanRepository.save(studyPlan);
        log.info("Created study plan: {} ({})", studyPlan.getCode(), studyPlan.getId());
        return toDTO(studyPlan);
    }

    @Transactional
    public StudyPlanDTO updateStudyPlan(String id, UpdateStudyPlanRequest request) {
        StudyPlan studyPlan = studyPlanRepository.findById(java.util.UUID.fromString(id))
                .orElseThrow(() -> new IllegalArgumentException("Plan de estudio no encontrado"));

        if (request.getCode() != null) {
            if (!studyPlan.getCode().equals(request.getCode()) &&
                    studyPlanRepository.existsByCodeAndIsDeletedFalseAndIdNot(request.getCode(), studyPlan.getId())) {
                throw new IllegalArgumentException("Ya existe un plan de estudio con ese código");
            }
            studyPlan.setCode(request.getCode().toUpperCase());
        }
        if (request.getName() != null) studyPlan.setName(request.getName());
        if (request.getVersion() != null) studyPlan.setVersion(request.getVersion());
        if (request.getDescription() != null) studyPlan.setDescription(request.getDescription());
        if (request.getTitleDegree() != null) studyPlan.setTitleDegree(request.getTitleDegree());
        if (request.getTotalCredits() != null) studyPlan.setTotalCredits(request.getTotalCredits());
        if (request.getDurationSemesters() != null) studyPlan.setDurationSemesters(request.getDurationSemesters());

        studyPlan = studyPlanRepository.save(studyPlan);
        log.info("Updated study plan: {} ({})", studyPlan.getCode(), studyPlan.getId());
        return toDTO(studyPlan);
    }

    @Transactional
    public void deleteStudyPlan(String id) {
        StudyPlan studyPlan = studyPlanRepository.findById(java.util.UUID.fromString(id))
                .orElseThrow(() -> new IllegalArgumentException("Plan de estudio no encontrado"));
        studyPlan.setIsDeleted(true);
        studyPlanRepository.save(studyPlan);
        log.info("Deleted study plan: {}", id);
    }

    private StudyPlanDTO toDTO(StudyPlan studyPlan) {
        return StudyPlanDTO.builder()
                .id(studyPlan.getId())
                .code(studyPlan.getCode())
                .name(studyPlan.getName())
                .version(studyPlan.getVersion())
                .description(studyPlan.getDescription())
                .titleDegree(studyPlan.getTitleDegree())
                .totalCredits(studyPlan.getTotalCredits())
                .durationSemesters(studyPlan.getDurationSemesters())
                .isActive(studyPlan.getIsActive())
                .isDeleted(studyPlan.getIsDeleted())
                .createdAt(studyPlan.getCreatedAt())
                .build();
    }
}
