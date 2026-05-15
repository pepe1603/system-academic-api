package com.academic_system.service;

import com.academic_system.dto.cpanel.*;
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
public class SemesterService {

    private final SemesterRepository semesterRepository;
    private final StudyPlanRepository studyPlanRepository;

    @Transactional(readOnly = true)
    public Page<SemesterDTO> getAllSemesters(Pageable pageable) {
        return semesterRepository.findAllByIsDeletedFalse(pageable)
                .map(this::toDTO);
    }

    @Transactional(readOnly = true)
    public Optional<SemesterDTO> getSemesterById(String id) {
        return semesterRepository.findById(UUID.fromString(id))
                .filter(s -> !Boolean.TRUE.equals(s.getIsDeleted()))
                .map(this::toDTO);
    }

    @Transactional(readOnly = true)
    public List<SemesterDTO> getSemestersByStudyPlan(String studyPlanId) {
        return semesterRepository.findByStudyPlanIdAndIsDeletedFalse(UUID.fromString(studyPlanId))
                .stream()
                .map(this::toDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<SemesterDTO> getDeletedSemesters(Pageable pageable) {
        return semesterRepository.findAllByIsDeletedTrue(pageable)
                .map(this::toDTO)
                .getContent();
    }

    @Transactional
    public SemesterDTO createSemester(CreateSemesterRequest request) {
        if (request.getStudyPlanId() != null) {
            studyPlanRepository.findById(request.getStudyPlanId())
                    .orElseThrow(() -> new IllegalArgumentException("Plan de estudio no encontrado"));
        }

        semesterRepository.findByStudyPlanIdAndSemesterNumberAndIsDeletedFalse(
                request.getStudyPlanId(), request.getSemesterNumber())
                .ifPresent(s -> {
                    throw new IllegalArgumentException("Ya existe el semestre " + request.getSemesterNumber()
                            + " para este plan de estudio");
                });

        Semester semester = Semester.builder()
                .studyPlanId(request.getStudyPlanId())
                .semesterNumber(request.getSemesterNumber())
                .name(request.getName())
                .build();

        semester = semesterRepository.save(semester);
        log.info("Created semester: {} ({})", semester.getName(), semester.getId());
        return toDTO(semester);
    }

    @Transactional
    public SemesterDTO updateSemester(String id, UpdateSemesterRequest request) {
        Semester semester = semesterRepository.findById(UUID.fromString(id))
                .orElseThrow(() -> new IllegalArgumentException("Semestre no encontrado"));

        if (request.getStudyPlanId() != null) {
            studyPlanRepository.findById(request.getStudyPlanId())
                    .orElseThrow(() -> new IllegalArgumentException("Plan de estudio no encontrado"));
            semester.setStudyPlanId(request.getStudyPlanId());
        }
        if (request.getSemesterNumber() != null) semester.setSemesterNumber(request.getSemesterNumber());
        if (request.getName() != null) semester.setName(request.getName());
        if (request.getIsActive() != null) semester.setIsActive(request.getIsActive());

        semester = semesterRepository.save(semester);
        log.info("Updated semester: {}", semester.getId());
        return toDTO(semester);
    }

    @Transactional
    public void deleteSemester(String id) {
        Semester semester = semesterRepository.findById(UUID.fromString(id))
                .orElseThrow(() -> new IllegalArgumentException("Semestre no encontrado"));
        semester.setIsDeleted(true);
        semesterRepository.save(semester);
        log.info("Deleted semester: {}", id);
    }

    private SemesterDTO toDTO(Semester semester) {
        SemesterDTO.SemesterDTOBuilder builder = SemesterDTO.builder()
                .id(semester.getId())
                .studyPlanId(semester.getStudyPlanId())
                .semesterNumber(semester.getSemesterNumber())
                .name(semester.getName())
                .isActive(semester.getIsActive())
                .isDeleted(semester.getIsDeleted());

        if (semester.getStudyPlanId() != null) {
            studyPlanRepository.findById(semester.getStudyPlanId()).ifPresent(sp ->
                    builder.studyPlanName(sp.getName()));
        }

        return builder.build();
    }
}
