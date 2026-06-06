package com.academic_system.service;

import com.academic_system.dto.cpanel.AcademicPeriodDTO;
import com.academic_system.dto.cpanel.CreateAcademicPeriodRequest;
import com.academic_system.dto.cpanel.UpdateAcademicPeriodRequest;
import com.academic_system.entity.postgres.AcademicPeriod;
import com.academic_system.exception.DuplicateResourceException;
import com.academic_system.exception.ResourceNotFoundException;
import com.academic_system.repository.postgres.AcademicPeriodRepository;
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
public class AcademicPeriodService {

    private final AcademicPeriodRepository academicPeriodRepository;

    @Transactional(readOnly = true)
    public Page<AcademicPeriodDTO> getAllAcademicPeriods(Pageable pageable) {
        return academicPeriodRepository.findAllByIsDeletedFalse(pageable)
                .map(this::toDTO);
    }

    @Transactional(readOnly = true)
    public Optional<AcademicPeriodDTO> getAcademicPeriodById(String id) {
        return academicPeriodRepository.findById(UUID.fromString(id))
                .filter(ap -> !Boolean.TRUE.equals(ap.getIsDeleted()))
                .map(this::toDTO);
    }

    @Transactional(readOnly = true)
    public List<AcademicPeriodDTO> getDeletedAcademicPeriods(Pageable pageable) {
        return academicPeriodRepository.findAllByIsDeletedTrue(pageable)
                .map(this::toDTO)
                .getContent();
    }

    @Transactional
    public AcademicPeriodDTO createAcademicPeriod(CreateAcademicPeriodRequest request) {
        if (academicPeriodRepository.existsByNameAndIsDeletedFalse(request.getName().toUpperCase())) {
            throw new DuplicateResourceException("Ya existe un período académico con ese nombre", "AcademicPeriod", "name");
        }

        AcademicPeriod academicPeriod = AcademicPeriod.builder()
                .name(request.getName().toUpperCase())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .build();

        academicPeriod = academicPeriodRepository.save(academicPeriod);
        log.info("Created academic period: {} ({})", academicPeriod.getName(), academicPeriod.getId());
        return toDTO(academicPeriod);
    }

    @Transactional
    public AcademicPeriodDTO updateAcademicPeriod(String id, UpdateAcademicPeriodRequest request) {
        AcademicPeriod academicPeriod = academicPeriodRepository.findById(UUID.fromString(id))
                .orElseThrow(() -> new ResourceNotFoundException("Período académico no encontrado", "AcademicPeriod", "id"));

        if (request.getName() != null) {
            String newName = request.getName().toUpperCase();
            if (!academicPeriod.getName().equals(newName) &&
                    academicPeriodRepository.existsByNameAndIsDeletedFalseAndIdNot(newName, academicPeriod.getId())) {
                throw new DuplicateResourceException("Ya existe un período académico con ese nombre", "AcademicPeriod", "name");
            }
            academicPeriod.setName(newName);
        }
        if (request.getStartDate() != null) academicPeriod.setStartDate(request.getStartDate());
        if (request.getEndDate() != null) academicPeriod.setEndDate(request.getEndDate());
        if (request.getIsActive() != null) academicPeriod.setIsActive(request.getIsActive());

        academicPeriod = academicPeriodRepository.save(academicPeriod);
        log.info("Updated academic period: {} ({})", academicPeriod.getName(), academicPeriod.getId());
        return toDTO(academicPeriod);
    }

    @Transactional
    public void deleteAcademicPeriod(String id) {
        AcademicPeriod academicPeriod = academicPeriodRepository.findById(UUID.fromString(id))
                .orElseThrow(() -> new ResourceNotFoundException("Período académico no encontrado", "AcademicPeriod", "id"));
        academicPeriod.setIsDeleted(true);
        academicPeriodRepository.save(academicPeriod);
        log.info("Deleted academic period: {}", id);
    }

    private AcademicPeriodDTO toDTO(AcademicPeriod academicPeriod) {
        return AcademicPeriodDTO.builder()
                .id(academicPeriod.getId())
                .name(academicPeriod.getName())
                .startDate(academicPeriod.getStartDate())
                .endDate(academicPeriod.getEndDate())
                .isActive(academicPeriod.getIsActive())
                .isDeleted(academicPeriod.getIsDeleted())
                .createdAt(academicPeriod.getCreatedAt())
                .build();
    }
}
