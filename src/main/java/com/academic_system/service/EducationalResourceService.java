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
public class EducationalResourceService {

    private final EducationalResourceRepository educationalResourceRepository;
    private final CourseRepository courseRepository;

    private static final List<String> VALID_TYPES = List.of("PDF", "VIDEO", "LINK", "DOCUMENT", "PRESENTATION");

    @Transactional(readOnly = true)
    public Page<EducationalResourceDTO> getAllResources(Pageable pageable) {
        return educationalResourceRepository.findAllByIsDeletedFalse(pageable)
                .map(this::toDTO);
    }

    @Transactional(readOnly = true)
    public Optional<EducationalResourceDTO> getResourceById(String id) {
        return educationalResourceRepository.findById(UUID.fromString(id))
                .filter(r -> !Boolean.TRUE.equals(r.getIsDeleted()))
                .map(this::toDTO);
    }

    @Transactional(readOnly = true)
    public List<EducationalResourceDTO> getResourcesByCourse(String courseId) {
        return educationalResourceRepository.findByCourseIdAndIsDeletedFalse(UUID.fromString(courseId))
                .stream()
                .map(this::toDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<EducationalResourceDTO> getDeletedResources(Pageable pageable) {
        return educationalResourceRepository.findAllByIsDeletedTrue(pageable)
                .map(this::toDTO)
                .getContent();
    }

    @Transactional
    public EducationalResourceDTO createResource(CreateEducationalResourceRequest request) {
        if (request.getCourseId() != null) {
            courseRepository.findById(request.getCourseId())
                    .orElseThrow(() -> new IllegalArgumentException("Curso no encontrado"));
        }

        String type = request.getResourceType().toUpperCase();
        if (!VALID_TYPES.contains(type)) {
            throw new IllegalArgumentException("Tipo de recurso inválido. Valores: PDF, VIDEO, LINK, DOCUMENT, PRESENTATION");
        }

        EducationalResource resource = EducationalResource.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .resourceType(type)
                .resourceUrl(request.getResourceUrl())
                .courseId(request.getCourseId())
                .build();

        resource = educationalResourceRepository.save(resource);
        log.info("Created resource: {} ({})", resource.getTitle(), resource.getId());
        return toDTO(resource);
    }

    @Transactional
    public EducationalResourceDTO updateResource(String id, UpdateEducationalResourceRequest request) {
        EducationalResource resource = educationalResourceRepository.findById(UUID.fromString(id))
                .orElseThrow(() -> new IllegalArgumentException("Recurso no encontrado"));

        if (request.getTitle() != null) resource.setTitle(request.getTitle());
        if (request.getDescription() != null) resource.setDescription(request.getDescription());
        if (request.getResourceType() != null) {
            String newType = request.getResourceType().toUpperCase();
            if (!VALID_TYPES.contains(newType)) throw new IllegalArgumentException("Tipo de recurso inválido");
            resource.setResourceType(newType);
        }
        if (request.getResourceUrl() != null) resource.setResourceUrl(request.getResourceUrl());
        if (request.getCourseId() != null) {
            courseRepository.findById(request.getCourseId())
                    .orElseThrow(() -> new IllegalArgumentException("Curso no encontrado"));
            resource.setCourseId(request.getCourseId());
        }
        if (request.getIsPublished() != null) resource.setIsPublished(request.getIsPublished());

        resource = educationalResourceRepository.save(resource);
        log.info("Updated resource: {}", resource.getId());
        return toDTO(resource);
    }

    @Transactional
    public void deleteResource(String id) {
        EducationalResource resource = educationalResourceRepository.findById(UUID.fromString(id))
                .orElseThrow(() -> new IllegalArgumentException("Recurso no encontrado"));
        resource.setIsDeleted(true);
        educationalResourceRepository.save(resource);
        log.info("Deleted resource: {}", id);
    }

    private EducationalResourceDTO toDTO(EducationalResource resource) {
        EducationalResourceDTO.EducationalResourceDTOBuilder builder = EducationalResourceDTO.builder()
                .id(resource.getId())
                .title(resource.getTitle())
                .description(resource.getDescription())
                .resourceType(resource.getResourceType())
                .resourceUrl(resource.getResourceUrl())
                .courseId(resource.getCourseId())
                .isPublished(resource.getIsPublished())
                .isDeleted(resource.getIsDeleted());

        if (resource.getCourseId() != null) {
            courseRepository.findById(resource.getCourseId()).ifPresent(c -> {
                builder.courseCode(c.getCourseCode());
                builder.courseName(c.getName());
            });
        }

        return builder.build();
    }
}
