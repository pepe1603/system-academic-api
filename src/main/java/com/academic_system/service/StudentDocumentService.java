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
public class StudentDocumentService {

    private final StudentDocumentRepository studentDocumentRepository;
    private final StudentRepository studentRepository;

    private static final List<String> VALID_DOCUMENT_TYPES = List.of(
            "CURP", "BIRTH_CERTIFICATE", "PHOTO", "HIGH_SCHOOL_CERTIFICATE",
            "HIGH_SCHOOL_KARDEX", "IDENTIFICATION", "PROOF_OF_ADDRESS", "PAYMENT", "OTHER");

    @Transactional(readOnly = true)
    public Page<StudentDocumentDTO> getAllDocuments(Pageable pageable) {
        return studentDocumentRepository.findAllByIsDeletedFalse(pageable)
                .map(this::toDTO);
    }

    @Transactional(readOnly = true)
    public Optional<StudentDocumentDTO> getDocumentById(String id) {
        return studentDocumentRepository.findById(UUID.fromString(id))
                .filter(d -> !Boolean.TRUE.equals(d.getIsDeleted()))
                .map(this::toDTO);
    }

    @Transactional(readOnly = true)
    public List<StudentDocumentDTO> getDocumentsByStudent(String studentId) {
        return studentDocumentRepository.findByStudentIdAndIsDeletedFalse(UUID.fromString(studentId))
                .stream()
                .map(this::toDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<StudentDocumentDTO> getDeletedDocuments(Pageable pageable) {
        return studentDocumentRepository.findAllByIsDeletedTrue(pageable)
                .map(this::toDTO)
                .getContent();
    }

    @Transactional
    public StudentDocumentDTO createDocument(CreateStudentDocumentRequest request) {
        Student student = studentRepository.findById(request.getStudentId())
                .orElseThrow(() -> new IllegalArgumentException("Estudiante no encontrado"));

        String docType = request.getDocumentType().toUpperCase();
        if (!VALID_DOCUMENT_TYPES.contains(docType)) {
            throw new IllegalArgumentException("Tipo de documento inválido. Valores: CURP, BIRTH_CERTIFICATE, PHOTO, HIGH_SCHOOL_CERTIFICATE, HIGH_SCHOOL_KARDEX, IDENTIFICATION, PROOF_OF_ADDRESS, PAYMENT, OTHER");
        }

        StudentDocument document = StudentDocument.builder()
                .studentId(student.getId())
                .documentType(docType)
                .originalName(request.getOriginalName())
                .fileName(request.getFileName())
                .filePath(request.getFilePath())
                .fileSizeBytes(request.getFileSizeBytes())
                .mimeType(request.getMimeType())
                .documentNumber(request.getDocumentNumber())
                .issueDate(request.getIssueDate())
                .expirationDate(request.getExpirationDate())
                .observations(request.getObservations())
                .build();

        document = studentDocumentRepository.save(document);
        log.info("Created document: {} for student {} ({})", docType, student.getEnrollmentNumber(), document.getId());
        return toDTO(document);
    }

    @Transactional
    public StudentDocumentDTO updateDocument(String id, UpdateStudentDocumentRequest request) {
        StudentDocument document = studentDocumentRepository.findById(UUID.fromString(id))
                .orElseThrow(() -> new IllegalArgumentException("Documento no encontrado"));

        if (request.getOriginalName() != null) document.setOriginalName(request.getOriginalName());
        if (request.getFileName() != null) document.setFileName(request.getFileName());
        if (request.getFilePath() != null) document.setFilePath(request.getFilePath());
        if (request.getFileSizeBytes() != null) document.setFileSizeBytes(request.getFileSizeBytes());
        if (request.getMimeType() != null) document.setMimeType(request.getMimeType());
        if (request.getDocumentNumber() != null) document.setDocumentNumber(request.getDocumentNumber());
        if (request.getIssueDate() != null) document.setIssueDate(request.getIssueDate());
        if (request.getExpirationDate() != null) document.setExpirationDate(request.getExpirationDate());
        if (request.getIsVerified() != null) document.setIsVerified(request.getIsVerified());
        if (request.getVerifiedBy() != null) document.setVerifiedBy(request.getVerifiedBy());
        if (request.getVerificationDate() != null) document.setVerificationDate(request.getVerificationDate());
        if (request.getObservations() != null) document.setObservations(request.getObservations());
        if (request.getIsActive() != null) document.setIsActive(request.getIsActive());

        document = studentDocumentRepository.save(document);
        log.info("Updated document: {}", document.getId());
        return toDTO(document);
    }

    @Transactional
    public void deleteDocument(String id) {
        StudentDocument document = studentDocumentRepository.findById(UUID.fromString(id))
                .orElseThrow(() -> new IllegalArgumentException("Documento no encontrado"));
        document.setIsDeleted(true);
        studentDocumentRepository.save(document);
        log.info("Deleted document: {}", id);
    }

    private StudentDocumentDTO toDTO(StudentDocument doc) {
        StudentDocumentDTO.StudentDocumentDTOBuilder builder = StudentDocumentDTO.builder()
                .id(doc.getId())
                .studentId(doc.getStudentId())
                .documentType(doc.getDocumentType())
                .originalName(doc.getOriginalName())
                .fileName(doc.getFileName())
                .filePath(doc.getFilePath())
                .fileSizeBytes(doc.getFileSizeBytes())
                .mimeType(doc.getMimeType())
                .documentNumber(doc.getDocumentNumber())
                .issueDate(doc.getIssueDate())
                .expirationDate(doc.getExpirationDate())
                .isVerified(doc.getIsVerified())
                .isActive(doc.getIsActive())
                .isDeleted(doc.getIsDeleted())
                .createdAt(doc.getCreatedAt());

        if (doc.getStudentId() != null) {
            studentRepository.findById(doc.getStudentId()).ifPresent(s -> {
                builder.studentName(s.getFirstName() + " " + s.getLastName());
                builder.enrollmentNumber(s.getEnrollmentNumber());
            });
        }

        return builder.build();
    }
}
