package com.academic_system.service;

import com.academic_system.dto.cpanel.*;
import com.academic_system.entity.postgres.*;
import com.academic_system.exception.DuplicateResourceException;
import com.academic_system.exception.ResourceNotFoundException;
import com.academic_system.exception.ValidationException;
import com.academic_system.repository.postgres.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class CertificateService {

    private final CertificateRepository certificateRepository;
    private final StudentRepository studentRepository;
    private final GenerationRepository generationRepository;
    private final TeacherRepository teacherRepository;

    private static final List<String> VALID_TYPES = List.of(
            "PARTIAL", "TOTAL", "TITLE", "DIPLOMA", "CONSTANCIA");

    private static final List<String> VALID_STATUSES = List.of(
            "REQUESTED", "IN_PROCESS", "ISSUED", "DELIVERED", "CANCELLED");

    @Transactional(readOnly = true)
    public Page<CertificateDTO> getAllCertificates(Pageable pageable) {
        return certificateRepository.findAllByIsDeletedFalse(pageable)
                .map(this::toDTO);
    }

    @Transactional(readOnly = true)
    public Optional<CertificateDTO> getCertificateById(String id) {
        return certificateRepository.findById(UUID.fromString(id))
                .filter(c -> !Boolean.TRUE.equals(c.getIsDeleted()))
                .map(this::toDTO);
    }

    @Transactional(readOnly = true)
    public List<CertificateDTO> getCertificatesByStudent(String studentId) {
        return certificateRepository.findByStudentIdAndIsDeletedFalse(UUID.fromString(studentId))
                .stream()
                .map(this::toDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<CertificateDTO> getDeletedCertificates(Pageable pageable) {
        return certificateRepository.findAllByIsDeletedTrue(pageable)
                .map(this::toDTO)
                .getContent();
    }

    @Transactional
    public CertificateDTO createCertificate(CreateCertificateRequest request) {
        Student student = studentRepository.findById(request.getStudentId())
                .orElseThrow(() -> new ResourceNotFoundException("Estudiante no encontrado", "Student", "id"));

        if (request.getGenerationId() != null) {
            generationRepository.findById(request.getGenerationId())
                    .orElseThrow(() -> new ResourceNotFoundException("Generación no encontrada", "Generation", "id"));
        }

        String type = request.getCertificateType().toUpperCase();
        if (!VALID_TYPES.contains(type)) {
            throw new ValidationException("Tipo de certificado inválido. Valores: PARTIAL, TOTAL, TITLE, DIPLOMA, CONSTANCIA", "Certificate", "certificateType");
        }

        if (request.getOfficialFolio() != null
                && certificateRepository.existsByOfficialFolioAndIsDeletedFalse(request.getOfficialFolio())) {
            throw new DuplicateResourceException("El folio oficial ya existe", "Certificate", "officialFolio");
        }

        if (request.getDirectorSigner() != null) {
            teacherRepository.findById(request.getDirectorSigner())
                    .orElseThrow(() -> new ResourceNotFoundException("Docente firmante (director) no encontrado", "Teacher", "id"));
        }

        if (request.getSecretarySigner() != null) {
            teacherRepository.findById(request.getSecretarySigner())
                    .orElseThrow(() -> new ResourceNotFoundException("Docente firmante (secretario) no encontrado", "Teacher", "id"));
        }

        Certificate certificate = Certificate.builder()
                .studentId(student.getId())
                .generationId(request.getGenerationId())
                .certificateType(type)
                .officialFolio(request.getOfficialFolio())
                .internalFolio(request.getInternalFolio())
                .series(request.getSeries())
                .finalAverage(request.getFinalAverage())
                .totalCredits(request.getTotalCredits())
                .totalSubjects(request.getTotalSubjects())
                .issueDate(request.getIssueDate() != null ? request.getIssueDate() : LocalDate.now())
                .directorSigner(request.getDirectorSigner())
                .secretarySigner(request.getSecretarySigner())
                .recordNumber(request.getRecordNumber())
                .recordBook(request.getRecordBook())
                .recordPage(request.getRecordPage())
                .observations(request.getObservations())
                .build();

        certificate = certificateRepository.save(certificate);
        log.info("Created certificate: {} for student {} ({})", certificate.getOfficialFolio(),
                student.getEnrollmentNumber(), certificate.getId());
        return toDTO(certificate);
    }

    @Transactional
    public CertificateDTO updateCertificate(String id, UpdateCertificateRequest request) {
        Certificate certificate = certificateRepository.findById(UUID.fromString(id))
                .orElseThrow(() -> new ResourceNotFoundException("Certificado no encontrado", "Certificate", "id"));

        if (request.getCertificateType() != null) {
            String newType = request.getCertificateType().toUpperCase();
            if (!VALID_TYPES.contains(newType)) {
                throw new ValidationException("Tipo de certificado inválido", "Certificate", "certificateType");
            }
            certificate.setCertificateType(newType);
        }
        if (request.getOfficialFolio() != null) {
            if (!request.getOfficialFolio().equals(certificate.getOfficialFolio())
                    && certificateRepository.existsByOfficialFolioAndIsDeletedFalse(request.getOfficialFolio())) {
                throw new DuplicateResourceException("El folio oficial ya está registrado por otro certificado", "Certificate", "officialFolio");
            }
            certificate.setOfficialFolio(request.getOfficialFolio());
        }
        if (request.getInternalFolio() != null) certificate.setInternalFolio(request.getInternalFolio());
        if (request.getSeries() != null) certificate.setSeries(request.getSeries());
        if (request.getFinalAverage() != null) certificate.setFinalAverage(request.getFinalAverage());
        if (request.getTotalCredits() != null) certificate.setTotalCredits(request.getTotalCredits());
        if (request.getTotalSubjects() != null) certificate.setTotalSubjects(request.getTotalSubjects());
        if (request.getIssueDate() != null) certificate.setIssueDate(request.getIssueDate());
        if (request.getDeliveryDate() != null) certificate.setDeliveryDate(request.getDeliveryDate());
        if (request.getStatus() != null) {
            String newStatus = request.getStatus().toUpperCase();
            if (!VALID_STATUSES.contains(newStatus)) {
                throw new ValidationException("Estado inválido. Valores: REQUESTED, IN_PROCESS, ISSUED, DELIVERED, CANCELLED", "Certificate", "status");
            }
            certificate.setStatus(newStatus);
        }
        if (request.getDirectorSigner() != null) {
            teacherRepository.findById(request.getDirectorSigner())
                    .orElseThrow(() -> new ResourceNotFoundException("Docente firmante (director) no encontrado", "Teacher", "id"));
            certificate.setDirectorSigner(request.getDirectorSigner());
        }
        if (request.getSecretarySigner() != null) {
            teacherRepository.findById(request.getSecretarySigner())
                    .orElseThrow(() -> new ResourceNotFoundException("Docente firmante (secretario) no encontrado", "Teacher", "id"));
            certificate.setSecretarySigner(request.getSecretarySigner());
        }
        if (request.getRecordNumber() != null) certificate.setRecordNumber(request.getRecordNumber());
        if (request.getRecordBook() != null) certificate.setRecordBook(request.getRecordBook());
        if (request.getRecordPage() != null) certificate.setRecordPage(request.getRecordPage());
        if (request.getObservations() != null) certificate.setObservations(request.getObservations());

        certificate = certificateRepository.save(certificate);
        log.info("Updated certificate: {}", certificate.getId());
        return toDTO(certificate);
    }

    @Transactional
    public void deleteCertificate(String id) {
        Certificate certificate = certificateRepository.findById(UUID.fromString(id))
                .orElseThrow(() -> new ResourceNotFoundException("Certificado no encontrado", "Certificate", "id"));
        certificate.setIsDeleted(true);
        certificateRepository.save(certificate);
        log.info("Deleted certificate: {}", id);
    }

    private CertificateDTO toDTO(Certificate certificate) {
        CertificateDTO.CertificateDTOBuilder builder = CertificateDTO.builder()
                .id(certificate.getId())
                .certificateType(certificate.getCertificateType())
                .officialFolio(certificate.getOfficialFolio())
                .internalFolio(certificate.getInternalFolio())
                .series(certificate.getSeries())
                .finalAverage(certificate.getFinalAverage())
                .totalCredits(certificate.getTotalCredits())
                .totalSubjects(certificate.getTotalSubjects())
                .issueDate(certificate.getIssueDate())
                .deliveryDate(certificate.getDeliveryDate())
                .status(certificate.getStatus())
                .recordNumber(certificate.getRecordNumber())
                .recordBook(certificate.getRecordBook())
                .recordPage(certificate.getRecordPage())
                .observations(certificate.getObservations())
                .isDeleted(certificate.getIsDeleted())
                .createdAt(certificate.getCreatedAt())
                .studentId(certificate.getStudentId())
                .generationId(certificate.getGenerationId())
                .directorSigner(certificate.getDirectorSigner())
                .secretarySigner(certificate.getSecretarySigner());

        if (certificate.getStudentId() != null) {
            studentRepository.findById(certificate.getStudentId()).ifPresent(s -> {
                builder.studentName(s.getFirstName() + " " + s.getLastName());
                builder.enrollmentNumber(s.getEnrollmentNumber());
            });
        }

        if (certificate.getGenerationId() != null) {
            generationRepository.findById(certificate.getGenerationId()).ifPresent(g ->
                    builder.generationName(g.getName()));
        }

        if (certificate.getDirectorSigner() != null) {
            teacherRepository.findById(certificate.getDirectorSigner()).ifPresent(t ->
                    builder.directorName(t.getFirstName() + " " + t.getLastName()));
        }

        if (certificate.getSecretarySigner() != null) {
            teacherRepository.findById(certificate.getSecretarySigner()).ifPresent(t ->
                    builder.secretaryName(t.getFirstName() + " " + t.getLastName()));
        }

        return builder.build();
    }
}
