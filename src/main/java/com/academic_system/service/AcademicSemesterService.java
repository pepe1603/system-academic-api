package com.academic_system.service;

import com.academic_system.dto.cpanel.AcademicSemesterDTO;
import com.academic_system.dto.cpanel.CreateAcademicSemesterRequest;
import com.academic_system.dto.cpanel.UpdateAcademicSemesterRequest;
import com.academic_system.entity.postgres.AcademicSemester;
import com.academic_system.repository.postgres.AcademicSemesterRepository;
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
public class AcademicSemesterService {

    private final AcademicSemesterRepository academicSemesterRepository;

    @Transactional(readOnly = true)
    public Page<AcademicSemesterDTO> getAllAcademicSemesters(Pageable pageable) {
        return academicSemesterRepository.findAllByIsDeletedFalse(pageable)
                .map(this::toDTO);
    }

    @Transactional(readOnly = true)
    public Optional<AcademicSemesterDTO> getAcademicSemesterById(String id) {
        return academicSemesterRepository.findById(UUID.fromString(id))
                .filter(as -> !Boolean.TRUE.equals(as.getIsDeleted()))
                .map(this::toDTO);
    }

    @Transactional(readOnly = true)
    public List<AcademicSemesterDTO> getDeletedAcademicSemesters(Pageable pageable) {
        return academicSemesterRepository.findAllByIsDeletedTrue(pageable)
                .map(this::toDTO)
                .getContent();
    }

    @Transactional
    public AcademicSemesterDTO createAcademicSemester(CreateAcademicSemesterRequest request) {
        if (academicSemesterRepository.existsByNameAndIsDeletedFalse(request.getName())) {
            throw new IllegalArgumentException("Ya existe un semestre académico con ese nombre");
        }

        AcademicSemester academicSemester = AcademicSemester.builder()
                .name(request.getName())
                .year(request.getYear())
                .period(request.getPeriod())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .classesStartDate(request.getClassesStartDate())
                .classesEndDate(request.getClassesEndDate())
                .enrollmentDeadline(request.getEnrollmentDeadline())
                .dropDeadline(request.getDropDeadline())
                .status(request.getStatus() != null ? request.getStatus() : "DRAFT")
                .isCurrent(request.getIsCurrent() != null ? request.getIsCurrent() : false)
                .build();

        academicSemester = academicSemesterRepository.save(academicSemester);
        log.info("Created academic semester: {} ({})", academicSemester.getName(), academicSemester.getId());
        return toDTO(academicSemester);
    }

    @Transactional
    public AcademicSemesterDTO updateAcademicSemester(String id, UpdateAcademicSemesterRequest request) {
        AcademicSemester academicSemester = academicSemesterRepository.findById(UUID.fromString(id))
                .orElseThrow(() -> new IllegalArgumentException("Semestre académico no encontrado"));

        if (request.getName() != null) {
            if (!academicSemester.getName().equals(request.getName()) &&
                    academicSemesterRepository.existsByNameAndIsDeletedFalseAndIdNot(request.getName(), academicSemester.getId())) {
                throw new IllegalArgumentException("Ya existe un semestre académico con ese nombre");
            }
            academicSemester.setName(request.getName());
        }
        if (request.getYear() != null) academicSemester.setYear(request.getYear());
        if (request.getPeriod() != null) academicSemester.setPeriod(request.getPeriod());
        if (request.getStartDate() != null) academicSemester.setStartDate(request.getStartDate());
        if (request.getEndDate() != null) academicSemester.setEndDate(request.getEndDate());
        if (request.getClassesStartDate() != null) academicSemester.setClassesStartDate(request.getClassesStartDate());
        if (request.getClassesEndDate() != null) academicSemester.setClassesEndDate(request.getClassesEndDate());
        if (request.getEnrollmentDeadline() != null) academicSemester.setEnrollmentDeadline(request.getEnrollmentDeadline());
        if (request.getDropDeadline() != null) academicSemester.setDropDeadline(request.getDropDeadline());
        if (request.getStatus() != null) academicSemester.setStatus(request.getStatus());
        if (request.getIsCurrent() != null) academicSemester.setIsCurrent(request.getIsCurrent());

        academicSemester = academicSemesterRepository.save(academicSemester);
        log.info("Updated academic semester: {} ({})", academicSemester.getName(), academicSemester.getId());
        return toDTO(academicSemester);
    }

    @Transactional
    public void deleteAcademicSemester(String id) {
        AcademicSemester academicSemester = academicSemesterRepository.findById(UUID.fromString(id))
                .orElseThrow(() -> new IllegalArgumentException("Semestre académico no encontrado"));
        academicSemester.setIsDeleted(true);
        academicSemesterRepository.save(academicSemester);
        log.info("Deleted academic semester: {}", id);
    }

    private AcademicSemesterDTO toDTO(AcademicSemester academicSemester) {
        return AcademicSemesterDTO.builder()
                .id(academicSemester.getId())
                .name(academicSemester.getName())
                .year(academicSemester.getYear())
                .period(academicSemester.getPeriod())
                .startDate(academicSemester.getStartDate())
                .endDate(academicSemester.getEndDate())
                .classesStartDate(academicSemester.getClassesStartDate())
                .classesEndDate(academicSemester.getClassesEndDate())
                .enrollmentDeadline(academicSemester.getEnrollmentDeadline())
                .dropDeadline(academicSemester.getDropDeadline())
                .status(academicSemester.getStatus())
                .isCurrent(academicSemester.getIsCurrent())
                .isDeleted(academicSemester.getIsDeleted())
                .createdAt(academicSemester.getCreatedAt())
                .build();
    }
}
