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
public class GuardianService {

    private final GuardianRepository guardianRepository;
    private final StudentRepository studentRepository;

    private static final List<String> VALID_RELATIONSHIPS =
            List.of("FATHER", "MOTHER", "GUARDIAN", "SIBLING", "OTHER");

    @Transactional(readOnly = true)
    public Page<GuardianDTO> getAllGuardians(Pageable pageable) {
        return guardianRepository.findAllByIsDeletedFalse(pageable)
                .map(this::toDTO);
    }

    @Transactional(readOnly = true)
    public Optional<GuardianDTO> getGuardianById(String id) {
        return guardianRepository.findById(UUID.fromString(id))
                .filter(g -> !Boolean.TRUE.equals(g.getIsDeleted()))
                .map(this::toDTO);
    }

    @Transactional(readOnly = true)
    public List<GuardianDTO> getGuardiansByStudent(String studentId) {
        return guardianRepository.findByStudentIdAndIsDeletedFalse(UUID.fromString(studentId))
                .stream()
                .map(this::toDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<GuardianDTO> getDeletedGuardians(Pageable pageable) {
        return guardianRepository.findAllByIsDeletedTrue(pageable)
                .map(this::toDTO)
                .getContent();
    }

    @Transactional
    public GuardianDTO createGuardian(CreateGuardianRequest request) {
        Student student = studentRepository.findById(request.getStudentId())
                .orElseThrow(() -> new IllegalArgumentException("Estudiante no encontrado"));

        String relationship = request.getRelationship().toUpperCase();
        if (!VALID_RELATIONSHIPS.contains(relationship)) {
            throw new IllegalArgumentException("Parentesco inválido. Valores: FATHER, MOTHER, GUARDIAN, SIBLING, OTHER");
        }

        Guardian guardian = Guardian.builder()
                .studentId(student.getId())
                .fullName(request.getFullName())
                .relationship(relationship)
                .curp(request.getCurp())
                .primaryPhone(request.getPrimaryPhone())
                .secondaryPhone(request.getSecondaryPhone())
                .email(request.getEmail())
                .occupation(request.getOccupation())
                .company(request.getCompany())
                .address(request.getAddress())
                .isEmergencyContact(request.getIsEmergencyContact() != null ? request.getIsEmergencyContact() : true)
                .build();

        guardian = guardianRepository.save(guardian);
        log.info("Created guardian: {} for student {}", guardian.getFullName(), student.getEnrollmentNumber());
        return toDTO(guardian);
    }

    @Transactional
    public GuardianDTO updateGuardian(String id, UpdateGuardianRequest request) {
        Guardian guardian = guardianRepository.findById(UUID.fromString(id))
                .orElseThrow(() -> new IllegalArgumentException("Tutor no encontrado"));

        if (request.getFullName() != null) guardian.setFullName(request.getFullName());
        if (request.getRelationship() != null) {
            String newRel = request.getRelationship().toUpperCase();
            if (!VALID_RELATIONSHIPS.contains(newRel)) {
                throw new IllegalArgumentException("Parentesco inválido");
            }
            guardian.setRelationship(newRel);
        }
        if (request.getCurp() != null) guardian.setCurp(request.getCurp());
        if (request.getPrimaryPhone() != null) guardian.setPrimaryPhone(request.getPrimaryPhone());
        if (request.getSecondaryPhone() != null) guardian.setSecondaryPhone(request.getSecondaryPhone());
        if (request.getEmail() != null) guardian.setEmail(request.getEmail());
        if (request.getOccupation() != null) guardian.setOccupation(request.getOccupation());
        if (request.getCompany() != null) guardian.setCompany(request.getCompany());
        if (request.getAddress() != null) guardian.setAddress(request.getAddress());
        if (request.getIsEmergencyContact() != null) guardian.setIsEmergencyContact(request.getIsEmergencyContact());
        if (request.getIsActive() != null) guardian.setIsActive(request.getIsActive());

        guardian = guardianRepository.save(guardian);
        log.info("Updated guardian: {}", guardian.getId());
        return toDTO(guardian);
    }

    @Transactional
    public void deleteGuardian(String id) {
        Guardian guardian = guardianRepository.findById(UUID.fromString(id))
                .orElseThrow(() -> new IllegalArgumentException("Tutor no encontrado"));
        guardian.setIsDeleted(true);
        guardianRepository.save(guardian);
        log.info("Deleted guardian: {}", id);
    }

    private GuardianDTO toDTO(Guardian guardian) {
        GuardianDTO.GuardianDTOBuilder builder = GuardianDTO.builder()
                .id(guardian.getId())
                .studentId(guardian.getStudentId())
                .fullName(guardian.getFullName())
                .relationship(guardian.getRelationship())
                .curp(guardian.getCurp())
                .primaryPhone(guardian.getPrimaryPhone())
                .secondaryPhone(guardian.getSecondaryPhone())
                .email(guardian.getEmail())
                .occupation(guardian.getOccupation())
                .company(guardian.getCompany())
                .address(guardian.getAddress())
                .isEmergencyContact(guardian.getIsEmergencyContact())
                .isActive(guardian.getIsActive())
                .isDeleted(guardian.getIsDeleted())
                .createdAt(guardian.getCreatedAt());

        if (guardian.getStudentId() != null) {
            studentRepository.findById(guardian.getStudentId()).ifPresent(s -> {
                builder.studentName(s.getFirstName() + " " + s.getLastName());
                builder.enrollmentNumber(s.getEnrollmentNumber());
            });
        }

        return builder.build();
    }
}
