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

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReportCardService {

    private final ReportCardRepository reportCardRepository;
    private final ReportCardDetailRepository reportCardDetailRepository;
    private final StudentRepository studentRepository;
    private final AcademicSemesterRepository academicSemesterRepository;
    private final GenerationRepository generationRepository;
    private final CourseRepository courseRepository;
    private final KardexRepository kardexRepository;

    private static final List<String> VALID_TYPES = List.of(
            "ORDINARY", "EXTRAORDINARY", "SPECIAL", "PARTIAL_CERTIFICATE", "FINAL_CERTIFICATE");

    private static final List<String> VALID_MODES = List.of("ONLINE", "OFFICIAL");

    private static final List<String> VALID_STATUSES = List.of(
            "PENDING", "ISSUED", "DELIVERED", "ARCHIVED", "CANCELLED");

    @Transactional(readOnly = true)
    public Page<ReportCardDTO> getAllReportCards(Pageable pageable) {
        Page<ReportCard> reportCards = reportCardRepository.findAllByIsDeletedFalse(pageable);

        Set<UUID> studentIds = reportCards.getContent().stream()
                .map(ReportCard::getStudentId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        Set<UUID> academicSemesterIds = reportCards.getContent().stream()
                .map(ReportCard::getAcademicSemesterId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        Set<UUID> generationIds = reportCards.getContent().stream()
                .map(ReportCard::getGenerationId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        Set<UUID> reportCardIds = reportCards.getContent().stream()
                .map(ReportCard::getId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        Map<UUID, Student> studentMap = studentRepository.findAllById(studentIds).stream()
                .collect(Collectors.toMap(Student::getId, s -> s));

        Map<UUID, AcademicSemester> semesterMap = academicSemesterRepository.findAllById(academicSemesterIds).stream()
                .collect(Collectors.toMap(AcademicSemester::getId, s -> s));

        Map<UUID, Generation> generationMap = generationRepository.findAllById(generationIds).stream()
                .collect(Collectors.toMap(Generation::getId, g -> g));

        Map<UUID, List<ReportCardDetail>> detailsMap = reportCardDetailRepository
                .findByReportCardIdIn(new ArrayList<>(reportCardIds)).stream()
                .collect(Collectors.groupingBy(ReportCardDetail::getReportCardId));

        return reportCards.map(rc -> toDTO(rc, studentMap, semesterMap, generationMap, detailsMap));
    }

    @Transactional(readOnly = true)
    public Optional<ReportCardDTO> getReportCardById(String id) {
        return reportCardRepository.findById(UUID.fromString(id))
                .filter(rc -> !Boolean.TRUE.equals(rc.getIsDeleted()))
                .map(rc -> toDTO(rc, Collections.emptyMap(), Collections.emptyMap(), Collections.emptyMap(), Collections.emptyMap()));
    }

    @Transactional(readOnly = true)
    public List<ReportCardDTO> getReportCardsByStudent(String studentId) {
        List<ReportCard> reportCards = reportCardRepository.findByStudentIdAndIsDeletedFalse(UUID.fromString(studentId));

        Set<UUID> studentIds = reportCards.stream()
                .map(ReportCard::getStudentId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        Set<UUID> academicSemesterIds = reportCards.stream()
                .map(ReportCard::getAcademicSemesterId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        Set<UUID> generationIds = reportCards.stream()
                .map(ReportCard::getGenerationId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        Set<UUID> reportCardIds = reportCards.stream()
                .map(ReportCard::getId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        Map<UUID, Student> studentMap = studentRepository.findAllById(studentIds).stream()
                .collect(Collectors.toMap(Student::getId, s -> s));

        Map<UUID, AcademicSemester> semesterMap = academicSemesterRepository.findAllById(academicSemesterIds).stream()
                .collect(Collectors.toMap(AcademicSemester::getId, s -> s));

        Map<UUID, Generation> generationMap = generationRepository.findAllById(generationIds).stream()
                .collect(Collectors.toMap(Generation::getId, g -> g));

        Map<UUID, List<ReportCardDetail>> detailsMap = reportCardDetailRepository
                .findByReportCardIdIn(new ArrayList<>(reportCardIds)).stream()
                .collect(Collectors.groupingBy(ReportCardDetail::getReportCardId));

        return reportCards.stream()
                .map(rc -> toDTO(rc, studentMap, semesterMap, generationMap, detailsMap))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ReportCardDTO> getDeletedReportCards(Pageable pageable) {
        Page<ReportCard> reportCards = reportCardRepository.findAllByIsDeletedTrue(pageable);

        Set<UUID> studentIds = reportCards.getContent().stream()
                .map(ReportCard::getStudentId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        Set<UUID> academicSemesterIds = reportCards.getContent().stream()
                .map(ReportCard::getAcademicSemesterId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        Set<UUID> generationIds = reportCards.getContent().stream()
                .map(ReportCard::getGenerationId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        Set<UUID> reportCardIds = reportCards.getContent().stream()
                .map(ReportCard::getId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        Map<UUID, Student> studentMap = studentRepository.findAllById(studentIds).stream()
                .collect(Collectors.toMap(Student::getId, s -> s));

        Map<UUID, AcademicSemester> semesterMap = academicSemesterRepository.findAllById(academicSemesterIds).stream()
                .collect(Collectors.toMap(AcademicSemester::getId, s -> s));

        Map<UUID, Generation> generationMap = generationRepository.findAllById(generationIds).stream()
                .collect(Collectors.toMap(Generation::getId, g -> g));

        Map<UUID, List<ReportCardDetail>> detailsMap = reportCardDetailRepository
                .findByReportCardIdIn(new ArrayList<>(reportCardIds)).stream()
                .collect(Collectors.groupingBy(ReportCardDetail::getReportCardId));

        return reportCards.getContent().stream()
                .map(rc -> toDTO(rc, studentMap, semesterMap, generationMap, detailsMap))
                .toList();
    }

    @Transactional
    public ReportCardDTO createReportCard(CreateReportCardRequest request) {
        Student student = studentRepository.findById(request.getStudentId())
                .orElseThrow(() -> new ResourceNotFoundException("Estudiante no encontrado", "Student", "id"));

        AcademicSemester academicSemester = academicSemesterRepository.findById(request.getAcademicSemesterId())
                .orElseThrow(() -> new ResourceNotFoundException("Semestre académico no encontrado", "AcademicSemester", "id"));

        if (request.getGenerationId() != null) {
            generationRepository.findById(request.getGenerationId())
                    .orElseThrow(() -> new ResourceNotFoundException("Generación no encontrada", "Generation", "id"));
        }

        String type = request.getReportCardType() != null ? request.getReportCardType().toUpperCase() : "ORDINARY";
        if (!VALID_TYPES.contains(type)) {
            throw new ValidationException("Tipo de boleta inválido. Valores: ORDINARY, EXTRAORDINARY, SPECIAL, PARTIAL_CERTIFICATE, FINAL_CERTIFICATE", "ReportCard", "reportCardType");
        }

        String mode = request.getGenerationMode() != null ? request.getGenerationMode().toUpperCase() : "ONLINE";
        if (!VALID_MODES.contains(mode)) {
            throw new ValidationException("Modo de generación inválido. Valores: ONLINE, OFFICIAL", "ReportCard", "generationMode");
        }

        if (request.getFolio() != null && reportCardRepository.existsByFolioAndIsDeletedFalse(request.getFolio())) {
            throw new DuplicateResourceException("El folio ya existe", "ReportCard", "folio");
        }

        ReportCard reportCard = ReportCard.builder()
                .studentId(student.getId())
                .academicSemesterId(academicSemester.getId())
                .generationId(request.getGenerationId())
                .reportCardType(type)
                .generationMode(mode)
                .folio(request.getFolio())
                .series(request.getSeries())
                .observations(request.getObservations())
                .build();

        reportCard = reportCardRepository.save(reportCard);

        List<ReportCardDetail> details = new ArrayList<>();
        for (CreateReportCardDetailRequest detailReq : request.getDetails()) {
            Course course = courseRepository.findById(detailReq.getCourseId())
                    .orElseThrow(() -> new ResourceNotFoundException("Curso no encontrado: " + detailReq.getCourseId(), "Course", "id"));

            if (detailReq.getKardexId() != null) {
                kardexRepository.findById(detailReq.getKardexId())
                        .orElseThrow(() -> new ResourceNotFoundException("Registro kardex no encontrado: " + detailReq.getKardexId(), "Kardex", "id"));
            }

            ReportCardDetail detail = ReportCardDetail.builder()
                    .reportCardId(reportCard.getId())
                    .kardexId(detailReq.getKardexId())
                    .courseId(course.getId())
                    .subjectName(detailReq.getSubjectName())
                    .subjectCode(detailReq.getSubjectCode())
                    .credits(detailReq.getCredits())
                    .grade(detailReq.getGrade())
                    .gradeLetter(detailReq.getGradeLetter())
                    .subjectStatus(detailReq.getSubjectStatus())
                    .attendancePercentage(detailReq.getAttendancePercentage())
                    .totalAttendances(detailReq.getTotalAttendances())
                    .classesAttended(detailReq.getClassesAttended())
                    .observations(detailReq.getObservations())
                    .build();

            details.add(detail);
        }

        List<ReportCardDetail> savedDetails = reportCardDetailRepository.saveAll(details);

        int totalCredits = savedDetails.stream().mapToInt(ReportCardDetail::getCredits).sum();
        long approvedCount = savedDetails.stream()
                .filter(d -> d.getGrade() != null && d.getGrade().compareTo(BigDecimal.valueOf(60)) >= 0)
                .count();
        int approvedCredits = savedDetails.stream()
                .filter(d -> d.getGrade() != null && d.getGrade().compareTo(BigDecimal.valueOf(60)) >= 0)
                .mapToInt(ReportCardDetail::getCredits)
                .sum();

        double avg = savedDetails.stream()
                .filter(d -> d.getGrade() != null)
                .mapToDouble(d -> d.getGrade().doubleValue())
                .average()
                .orElse(0.0);

        reportCard.setTotalSubjects(savedDetails.size());
        reportCard.setTotalSubjectsApproved((int) approvedCount);
        reportCard.setTotalCreditsEnrolled(totalCredits);
        reportCard.setTotalCreditsApproved(approvedCredits);
        reportCard.setOverallAverage(BigDecimal.valueOf(Math.round(avg * 100.0) / 100.0));

        if (avg >= 90) reportCard.setAverageLetter("A");
        else if (avg >= 80) reportCard.setAverageLetter("B");
        else if (avg >= 70) reportCard.setAverageLetter("C");
        else if (avg >= 60) reportCard.setAverageLetter("D");
        else reportCard.setAverageLetter("F");

        reportCardRepository.save(reportCard);

        log.info("Created report card: {} for student {} ({})", reportCard.getFolio(), student.getEnrollmentNumber(), reportCard.getId());
        return toDTO(reportCard, Collections.emptyMap(), Collections.emptyMap(), Collections.emptyMap(), Collections.emptyMap());
    }

    @Transactional
    public ReportCardDTO updateReportCard(String id, UpdateReportCardRequest request) {
        ReportCard reportCard = reportCardRepository.findById(UUID.fromString(id))
                .orElseThrow(() -> new ResourceNotFoundException("Boleta no encontrada", "ReportCard", "id"));

        if (request.getReportCardType() != null) {
            String newType = request.getReportCardType().toUpperCase();
            if (!VALID_TYPES.contains(newType)) {
                throw new ValidationException("Tipo de boleta inválido", "ReportCard", "reportCardType");
            }
            reportCard.setReportCardType(newType);
        }
        if (request.getGenerationMode() != null) {
            String newMode = request.getGenerationMode().toUpperCase();
            if (!VALID_MODES.contains(newMode)) {
                throw new ValidationException("Modo de generación inválido", "ReportCard", "generationMode");
            }
            reportCard.setGenerationMode(newMode);
        }
        if (request.getOverallAverage() != null) reportCard.setOverallAverage(request.getOverallAverage());
        if (request.getAverageLetter() != null) reportCard.setAverageLetter(request.getAverageLetter());
        if (request.getAttendanceAverage() != null) reportCard.setAttendanceAverage(request.getAttendanceAverage());
        if (request.getTotalCreditsEnrolled() != null) reportCard.setTotalCreditsEnrolled(request.getTotalCreditsEnrolled());
        if (request.getTotalCreditsApproved() != null) reportCard.setTotalCreditsApproved(request.getTotalCreditsApproved());
        if (request.getTotalSubjects() != null) reportCard.setTotalSubjects(request.getTotalSubjects());
        if (request.getTotalSubjectsApproved() != null) reportCard.setTotalSubjectsApproved(request.getTotalSubjectsApproved());
        if (request.getStatus() != null) {
            String newStatus = request.getStatus().toUpperCase();
            if (!VALID_STATUSES.contains(newStatus)) {
                throw new ValidationException("Estado inválido. Valores: PENDING, ISSUED, DELIVERED, ARCHIVED, CANCELLED", "ReportCard", "status");
            }
            reportCard.setStatus(newStatus);
        }
        if (request.getDeliveryDate() != null) reportCard.setDeliveryDate(request.getDeliveryDate());
        if (request.getFolio() != null) reportCard.setFolio(request.getFolio());
        if (request.getSeries() != null) reportCard.setSeries(request.getSeries());
        if (request.getObservations() != null) reportCard.setObservations(request.getObservations());
        if (request.getIsSigned() != null) reportCard.setIsSigned(request.getIsSigned());
        if (request.getSignedAt() != null) reportCard.setSignedAt(request.getSignedAt());
        if (request.getSignedSealUrl() != null) reportCard.setSignedSealUrl(request.getSignedSealUrl());

        reportCard = reportCardRepository.save(reportCard);
        log.info("Updated report card: {}", reportCard.getId());
        return toDTO(reportCard, Collections.emptyMap(), Collections.emptyMap(), Collections.emptyMap(), Collections.emptyMap());
    }

    @Transactional
    public void deleteReportCard(String id) {
        ReportCard reportCard = reportCardRepository.findById(UUID.fromString(id))
                .orElseThrow(() -> new ResourceNotFoundException("Boleta no encontrada", "ReportCard", "id"));
        reportCard.setIsDeleted(true);
        reportCardRepository.save(reportCard);
        log.info("Deleted report card: {}", id);
    }

    private ReportCardDTO toDTO(ReportCard reportCard, Map<UUID, Student> studentMap,
                                Map<UUID, AcademicSemester> semesterMap, Map<UUID, Generation> generationMap,
                                Map<UUID, List<ReportCardDetail>> detailsMap) {
        ReportCardDTO.ReportCardDTOBuilder builder = ReportCardDTO.builder()
                .id(reportCard.getId())
                .reportCardType(reportCard.getReportCardType())
                .generationMode(reportCard.getGenerationMode())
                .overallAverage(reportCard.getOverallAverage())
                .averageLetter(reportCard.getAverageLetter())
                .attendanceAverage(reportCard.getAttendanceAverage())
                .totalCreditsEnrolled(reportCard.getTotalCreditsEnrolled())
                .totalCreditsApproved(reportCard.getTotalCreditsApproved())
                .totalSubjects(reportCard.getTotalSubjects())
                .totalSubjectsApproved(reportCard.getTotalSubjectsApproved())
                .status(reportCard.getStatus())
                .issueDate(reportCard.getIssueDate())
                .deliveryDate(reportCard.getDeliveryDate())
                .folio(reportCard.getFolio())
                .series(reportCard.getSeries())
                .observations(reportCard.getObservations())
                .isSigned(reportCard.getIsSigned())
                .isDeleted(reportCard.getIsDeleted())
                .createdAt(reportCard.getCreatedAt())
                .studentId(reportCard.getStudentId())
                .academicSemesterId(reportCard.getAcademicSemesterId())
                .generationId(reportCard.getGenerationId());

        if (reportCard.getStudentId() != null) {
            Student student = studentMap.get(reportCard.getStudentId());
            if (student == null && studentMap.isEmpty()) {
                student = studentRepository.findById(reportCard.getStudentId()).orElse(null);
            }
            if (student != null) {
                Student s = student;
                builder.studentName(s.getFirstName() + " " + s.getLastName());
                builder.enrollmentNumber(s.getEnrollmentNumber());
            }
        }

        if (reportCard.getAcademicSemesterId() != null) {
            AcademicSemester semester = semesterMap.get(reportCard.getAcademicSemesterId());
            if (semester == null && semesterMap.isEmpty()) {
                semester = academicSemesterRepository.findById(reportCard.getAcademicSemesterId()).orElse(null);
            }
            if (semester != null) {
                builder.academicSemesterName(semester.getName());
            }
        }

        if (reportCard.getGenerationId() != null) {
            Generation generation = generationMap.get(reportCard.getGenerationId());
            if (generation == null && generationMap.isEmpty()) {
                generation = generationRepository.findById(reportCard.getGenerationId()).orElse(null);
            }
            if (generation != null) {
                builder.generationName(generation.getName());
            }
        }

        List<ReportCardDetail> details = detailsMap.get(reportCard.getId());
        if (details == null && detailsMap.isEmpty()) {
            details = reportCardDetailRepository.findByReportCardId(reportCard.getId());
        }
        if (details == null) {
            details = Collections.emptyList();
        }
        builder.details(details.stream().map(this::toDetailDTO).toList());

        return builder.build();
    }

    private ReportCardDetailDTO toDetailDTO(ReportCardDetail detail) {
        return ReportCardDetailDTO.builder()
                .id(detail.getId())
                .reportCardId(detail.getReportCardId())
                .kardexId(detail.getKardexId())
                .courseId(detail.getCourseId())
                .subjectName(detail.getSubjectName())
                .subjectCode(detail.getSubjectCode())
                .credits(detail.getCredits())
                .grade(detail.getGrade())
                .gradeLetter(detail.getGradeLetter())
                .subjectStatus(detail.getSubjectStatus())
                .attendancePercentage(detail.getAttendancePercentage())
                .totalAttendances(detail.getTotalAttendances())
                .classesAttended(detail.getClassesAttended())
                .observations(detail.getObservations())
                .build();
    }
}
