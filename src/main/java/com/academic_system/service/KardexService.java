package com.academic_system.service;

import com.academic_system.dto.cpanel.CreateKardexRequest;
import com.academic_system.dto.cpanel.KardexDTO;
import com.academic_system.dto.cpanel.UpdateKardexRequest;
import com.academic_system.entity.postgres.*;
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
public class KardexService {

    private final KardexRepository kardexRepository;
    private final StudentRepository studentRepository;
    private final CourseRepository courseRepository;
    private final AcademicSemesterRepository academicSemesterRepository;
    private final EnrollmentRepository enrollmentRepository;

    private static final List<String> VALID_STATUSES = List.of(
            "ENROLLED", "APPROVED", "FAILED", "EXTRAORDINARY", "DROPPED", "VALIDATED", "EQUIVALENCE");

    @Transactional(readOnly = true)
    public Page<KardexDTO> getAllKardexRecords(Pageable pageable) {
        return kardexRepository.findAllByIsDeletedFalse(pageable)
                .map(this::toDTO);
    }

    @Transactional(readOnly = true)
    public Optional<KardexDTO> getKardexById(String id) {
        return kardexRepository.findById(UUID.fromString(id))
                .filter(k -> !Boolean.TRUE.equals(k.getIsDeleted()))
                .map(this::toDTO);
    }

    @Transactional(readOnly = true)
    public List<KardexDTO> getKardexByStudent(String studentId) {
        return kardexRepository.findByStudentIdAndIsDeletedFalse(UUID.fromString(studentId))
                .stream()
                .map(this::toDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<KardexDTO> getDeletedKardexRecords(Pageable pageable) {
        return kardexRepository.findAllByIsDeletedTrue(pageable)
                .map(this::toDTO)
                .getContent();
    }

    @Transactional
    public KardexDTO createKardex(CreateKardexRequest request) {
        Student student = studentRepository.findById(request.getStudentId())
                .orElseThrow(() -> new IllegalArgumentException("Estudiante no encontrado"));

        Course course = courseRepository.findById(request.getCourseId())
                .orElseThrow(() -> new IllegalArgumentException("Curso no encontrado"));

        AcademicSemester academicSemester = academicSemesterRepository.findById(request.getAcademicSemesterId())
                .orElseThrow(() -> new IllegalArgumentException("Semestre académico no encontrado"));

        int attempt = request.getAttemptNumber() != null ? request.getAttemptNumber() : 1;
        if (kardexRepository.existsByStudentIdAndCourseIdAndAcademicSemesterIdAndAttemptNumberAndIsDeletedFalse(
                request.getStudentId(), request.getCourseId(), request.getAcademicSemesterId(), attempt)) {
            throw new IllegalArgumentException("Ya existe un registro kardex para este estudiante, curso, semestre e intento");
        }

        if (request.getEnrollmentId() != null) {
            enrollmentRepository.findById(request.getEnrollmentId())
                    .orElseThrow(() -> new IllegalArgumentException("Inscripción no encontrada"));
        }

        String status = request.getStatus() != null ? request.getStatus().toUpperCase() : "ENROLLED";
        if (!VALID_STATUSES.contains(status)) {
            throw new IllegalArgumentException("Estado inválido. Valores: ENROLLED, APPROVED, FAILED, EXTRAORDINARY, DROPPED, VALIDATED, EQUIVALENCE");
        }

        Kardex kardex = Kardex.builder()
                .studentId(student.getId())
                .courseId(course.getId())
                .academicSemesterId(academicSemester.getId())
                .enrollmentId(request.getEnrollmentId())
                .finalGrade(request.getFinalGrade())
                .letterGrade(request.getLetterGrade())
                .status(status)
                .attemptNumber(attempt)
                .enrollmentDate(request.getEnrollmentDate() != null ? request.getEnrollmentDate() : LocalDate.now())
                .observations(request.getObservations())
                .build();

        kardex = kardexRepository.save(kardex);
        log.info("Created kardex record: {} - {} ({})", student.getEnrollmentNumber(), course.getCourseCode(), kardex.getId());
        return toDTO(kardex);
    }

    @Transactional
    public KardexDTO updateKardex(String id, UpdateKardexRequest request) {
        Kardex kardex = kardexRepository.findById(UUID.fromString(id))
                .orElseThrow(() -> new IllegalArgumentException("Registro kardex no encontrado"));

        if (request.getStatus() != null) {
            String newStatus = request.getStatus().toUpperCase();
            if (!VALID_STATUSES.contains(newStatus)) {
                throw new IllegalArgumentException("Estado inválido. Valores: ENROLLED, APPROVED, FAILED, EXTRAORDINARY, DROPPED, VALIDATED, EQUIVALENCE");
            }
            kardex.setStatus(newStatus);
        }
        if (request.getFinalGrade() != null) kardex.setFinalGrade(request.getFinalGrade());
        if (request.getLetterGrade() != null) kardex.setLetterGrade(request.getLetterGrade());
        if (request.getAttemptNumber() != null) kardex.setAttemptNumber(request.getAttemptNumber());
        if (request.getApprovalDate() != null) kardex.setApprovalDate(request.getApprovalDate());
        if (request.getOfficialFolio() != null) kardex.setOfficialFolio(request.getOfficialFolio());
        if (request.getKardexFolio() != null) kardex.setKardexFolio(request.getKardexFolio());
        if (request.getKardexSequence() != null) kardex.setKardexSequence(request.getKardexSequence());
        if (request.getIsOfficialized() != null) kardex.setIsOfficialized(request.getIsOfficialized());
        if (request.getObservations() != null) kardex.setObservations(request.getObservations());

        kardex = kardexRepository.save(kardex);
        log.info("Updated kardex record: {}", kardex.getId());
        return toDTO(kardex);
    }

    @Transactional
    public void deleteKardex(String id) {
        Kardex kardex = kardexRepository.findById(UUID.fromString(id))
                .orElseThrow(() -> new IllegalArgumentException("Registro kardex no encontrado"));
        kardex.setIsDeleted(true);
        kardexRepository.save(kardex);
        log.info("Deleted kardex record: {}", id);
    }

    private KardexDTO toDTO(Kardex kardex) {
        KardexDTO.KardexDTOBuilder builder = KardexDTO.builder()
                .id(kardex.getId())
                .finalGrade(kardex.getFinalGrade())
                .letterGrade(kardex.getLetterGrade())
                .status(kardex.getStatus())
                .attemptNumber(kardex.getAttemptNumber())
                .enrollmentDate(kardex.getEnrollmentDate())
                .approvalDate(kardex.getApprovalDate())
                .officialFolio(kardex.getOfficialFolio())
                .kardexFolio(kardex.getKardexFolio())
                .kardexSequence(kardex.getKardexSequence())
                .isOfficialized(kardex.getIsOfficialized())
                .observations(kardex.getObservations())
                .isDeleted(kardex.getIsDeleted())
                .createdAt(kardex.getCreatedAt())
                .studentId(kardex.getStudentId())
                .courseId(kardex.getCourseId())
                .academicSemesterId(kardex.getAcademicSemesterId())
                .enrollmentId(kardex.getEnrollmentId());

        if (kardex.getStudentId() != null) {
            studentRepository.findById(kardex.getStudentId()).ifPresent(s -> {
                builder.studentName(s.getFirstName() + " " + s.getLastName());
                builder.enrollmentNumber(s.getEnrollmentNumber());
            });
        }

        if (kardex.getCourseId() != null) {
            courseRepository.findById(kardex.getCourseId()).ifPresent(c -> {
                builder.courseCode(c.getCourseCode());
                builder.courseName(c.getName());
                builder.courseCredits(c.getCredits());
            });
        }

        if (kardex.getAcademicSemesterId() != null) {
            academicSemesterRepository.findById(kardex.getAcademicSemesterId()).ifPresent(as ->
                    builder.academicSemesterName(as.getName()));
        }

        return builder.build();
    }
}
