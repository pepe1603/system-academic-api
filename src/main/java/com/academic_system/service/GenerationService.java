package com.academic_system.service;

import com.academic_system.dto.cpanel.CreateGenerationRequest;
import com.academic_system.dto.cpanel.GenerationDTO;
import com.academic_system.dto.cpanel.UpdateGenerationRequest;
import com.academic_system.entity.postgres.Generation;
import com.academic_system.repository.postgres.GenerationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class GenerationService {

    private final GenerationRepository generationRepository;

    @Transactional(readOnly = true)
    public Page<GenerationDTO> getAllGenerations(Pageable pageable) {
        return generationRepository.findAllByIsDeletedFalse(pageable)
                .map(this::toDTO);
    }

    @Transactional(readOnly = true)
    public Optional<GenerationDTO> getGenerationById(String id) {
        return generationRepository.findById(java.util.UUID.fromString(id))
                .filter(g -> !Boolean.TRUE.equals(g.getIsDeleted()))
                .map(this::toDTO);
    }

    @Transactional(readOnly = true)
    public java.util.List<GenerationDTO> getDeletedGenerations(Pageable pageable) {
        return generationRepository.findAllByIsDeletedTrue(pageable)
                .map(this::toDTO)
                .getContent();
    }

    @Transactional
    public GenerationDTO createGeneration(CreateGenerationRequest request) {
        if (generationRepository.existsByNameAndIsDeletedFalse(request.getName())) {
            throw new IllegalArgumentException("Ya existe una generación con ese nombre");
        }

        Generation generation = Generation.builder()
                .name(request.getName())
                .entryYear(request.getEntryYear())
                .graduationYear(request.getGraduationYear())
                .status(request.getStatus())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .build();

        generation = generationRepository.save(generation);
        log.info("Created generation: {}", generation.getId());
        return toDTO(generation);
    }

    @Transactional
    public GenerationDTO updateGeneration(String id, UpdateGenerationRequest request) {
        Generation generation = generationRepository.findById(java.util.UUID.fromString(id))
                .orElseThrow(() -> new IllegalArgumentException("Generación no encontrada"));

        if (request.getName() != null) {
            if (!generation.getName().equals(request.getName()) &&
                    generationRepository.existsByNameAndIsDeletedFalseAndIdNot(request.getName(), generation.getId())) {
                throw new IllegalArgumentException("Ya existe una generación con ese nombre");
            }
            generation.setName(request.getName());
        }
        if (request.getEntryYear() != null) generation.setEntryYear(request.getEntryYear());
        if (request.getGraduationYear() != null) generation.setGraduationYear(request.getGraduationYear());
        if (request.getStatus() != null) generation.setStatus(request.getStatus());
        if (request.getStartDate() != null) generation.setStartDate(request.getStartDate());
        if (request.getEndDate() != null) generation.setEndDate(request.getEndDate());

        generation = generationRepository.save(generation);
        log.info("Updated generation: {}", generation.getId());
        return toDTO(generation);
    }

    @Transactional
    public void deleteGeneration(String id) {
        Generation generation = generationRepository.findById(java.util.UUID.fromString(id))
                .orElseThrow(() -> new IllegalArgumentException("Generación no encontrada"));
        generation.setIsDeleted(true);
        generationRepository.save(generation);
        log.info("Deleted generation: {}", id);
    }

    private GenerationDTO toDTO(Generation generation) {
        return GenerationDTO.builder()
                .id(generation.getId())
                .name(generation.getName())
                .entryYear(generation.getEntryYear())
                .graduationYear(generation.getGraduationYear())
                .status(generation.getStatus())
                .startDate(generation.getStartDate())
                .endDate(generation.getEndDate())
                .isActive(generation.getIsActive())
                .isDeleted(generation.getIsDeleted())
                .createdAt(generation.getCreatedAt())
                .build();
    }
}
