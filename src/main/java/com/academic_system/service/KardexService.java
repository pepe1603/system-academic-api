package com.academic_system.service;

import com.academic_system.dto.cpanel.CreateKardexRequest;
import com.academic_system.dto.cpanel.KardexDTO;
import com.academic_system.dto.cpanel.UpdateKardexRequest;
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
import java.util.*;
import java.util.stream.Collectors;

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
        Page<Kardex> page = kardexRepository.findAllByIsDeletedFalse(pageable);
        List<Kardex> records = page.getContent();

        Set<UUID> studentIds = records.stream().map(Kardex::getStudentId).filter(Objects::nonNull).collect(Collectors.toSet());
        Set<UUID> courseIds = records.stream().map(Kardex::getCourseId).filter(Objects::nonNull).collect(Collectors.toSet());
        Set<UUID> semesterIds = records.stream().map(Kardex::getAcademicSemesterId).filter(Objects::nonNull).collect(Collectors.toSet());

        Map<UUID, Student> studentMap = studentRepository.findAllById(studentIds).stream().collect(Collectors.toMap(Student::getId, s -> s));
        Map<UUID, Course> courseMap = courseRepository.findAllById(courseIds).stream().collect(Collectors.toMap(Course::getId, c -> c));
        Map<UUID, AcademicSemester> semesterMap = academicSemesterRepository.findAllById(semesterIds).stream().collect(Collectors.toMap(AcademicSemester::getId, as -> as));

        return page.map(k -> toDTO(k, studentMap, courseMap, semesterMap));
    }

    @Transactional(readOnly = true)
    public Optional<KardexDTO> getKardexById(String id) {
        return kardexRepository.findById(UUID.fromString(id))
                .filter(k -> !Boolean.TRUE.equals(k.getIsDeleted()))
                .map(k -> toDTO(k, Collections.emptyMap(), Collections.emptyMap(), Collections.emptyMap()));
    }

    @Transactional(readOnly = true)
    public List<KardexDTO> getKardexByStudent(String studentId) {
        List<Kardex> records = kardexRepository.findByStudentIdAndIsDeletedFalse(UUID.fromString(studentId));

        Set<UUID> studentIds = records.stream().map(Kardex::getStudentId).filter(Objects::nonNull).collect(Collectors.toSet());
        Set<UUID> courseIds = records.stream().map(Kardex::getCourseId).filter(Objects::nonNull).collect(Collectors.toSet());
        Set<UUID> semesterIds = records.stream().map(Kardex::getAcademicSemesterId).filter(Objects::nonNull).collect(Collectors.toSet());

        Map<UUID, Student> studentMap = studentRepository.findAllById(studentIds).stream().collect(Collectors.toMap(Student::getId, s -> s));
        Map<UUID, Course> courseMap = courseRepository.findAllById(courseIds).stream().collect(Collectors.toMap(Course::getId, c -> c));
        Map<UUID, AcademicSemester> semesterMap = academicSemesterRepository.findAllById(semesterIds).stream().collect(Collectors.toMap(AcademicSemester::getId, as -> as));

        return records.stream()
                .map(k -> toDTO(k, studentMap, courseMap, semesterMap))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<KardexDTO> getDeletedKardexRecords(Pageable pageable) {
        Page<Kardex> page = kardexRepository.findAllByIsDeletedTrue(pageable);
        List<Kardex> records = page.getContent();

        Set<UUID> studentIds = records.stream().map(Kardex::getStudentId).filter(Objects::nonNull).collect(Collectors.toSet());
        Set<UUID> courseIds = records.stream().map(Kardex::getCourseId).filter(Objects::nonNull).collect(Collectors.toSet());
        Set<UUID> semesterIds = records.stream().map(Kardex::getAcademicSemesterId).filter(Objects::nonNull).collect(Collectors.toSet());

        Map<UUID, Student> studentMap = studentRepository.findAllById(studentIds).stream().collect(Collectors.toMap(Student::getId, s -> s));
        Map<UUID, Course> courseMap = courseRepository.findAllById(courseIds).stream().collect(Collectors.toMap(Course::getId, c -> c));
        Map<UUID, AcademicSemester> semesterMap = academicSemesterRepository.findAllById(semesterIds).stream().collect(Collectors.toMap(AcademicSemester::getId, as -> as));

        return records.stream()
                .map(k -> toDTO(k, studentMap, courseMap, semesterMap))
                .toList();
    }

    @Transactional
    public KardexDTO createKardex(CreateKardexRequest request) {
        Student student = studentRepository.findById(request.getStudentId())
                .orElseThrow(() -> new ResourceNotFoundException("Estudiante no encontrado", "Student", "id"));

        Course course = courseRepository.findById(request.getCourseId())
                .orElseThrow(() -> new ResourceNotFoundException("Curso no encontrado", "Course", "id"));

        AcademicSemester academicSemester = academicSemesterRepository.findById(request.getAcademicSemesterId())
                .orElseThrow(() -> new ResourceNotFoundException("Semestre académico no encontrado", "AcademicSemester", "id"));

        int attempt = request.getAttemptNumber() != null ? request.getAttemptNumber() : 1;
        if (kardexRepository.existsByStudentIdAndCourseIdAndAcademicSemesterIdAndAttemptNumberAndIsDeletedFalse(
                request.getStudentId(), request.getCourseId(), request.getAcademicSemesterId(), attempt)) {
            throw new DuplicateResourceException("Ya existe un registro kardex para este estudiante, curso, semestre e intento", "Kardex");
        }

        if (request.getEnrollmentId() != null) {
            enrollmentRepository.findById(request.getEnrollmentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Inscripción no encontrada", "Enrollment", "id"));
        }

        String status = request.getStatus() != null ? request.getStatus().toUpperCase() : "ENROLLED";
        if (!VALID_STATUSES.contains(status)) {
            throw new ValidationException("Estado inválido. Valores: ENROLLED, APPROVED, FAILED, EXTRAORDINARY, DROPPED, VALIDATED, EQUIVALENCE", "Kardex", "status");
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
        return toDTO(kardex, Collections.emptyMap(), Collections.emptyMap(), Collections.emptyMap());
    }

    @Transactional
    public KardexDTO updateKardex(String id, UpdateKardexRequest request) {
        Kardex kardex = kardexRepository.findById(UUID.fromString(id))
                .orElseThrow(() -> new ResourceNotFoundException("Registro kardex no encontrado", "Kardex", "id"));

        if (request.getStatus() != null) {
            String newStatus = request.getStatus().toUpperCase();
            if (!VALID_STATUSES.contains(newStatus)) {
                throw new ValidationException("Estado inválido. Valores: ENROLLED, APPROVED, FAILED, EXTRAORDINARY, DROPPED, VALIDATED, EQUIVALENCE", "Kardex", "status");
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
        return toDTO(kardex, Collections.emptyMap(), Collections.emptyMap(), Collections.emptyMap());
    }

    @Transactional
    public void deleteKardex(String id) {
        Kardex kardex = kardexRepository.findById(UUID.fromString(id))
                .orElseThrow(() -> new ResourceNotFoundException("Registro kardex no encontrado", "Kardex", "id"));
        kardex.setIsDeleted(true);
        kardexRepository.save(kardex);
        log.info("Deleted kardex record: {}", id);
    }

    private KardexDTO toDTO(Kardex kardex, Map<UUID, Student> studentMap,
                            Map<UUID, Course> courseMap, Map<UUID, AcademicSemester> semesterMap) {
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
            Student s = studentMap.get(kardex.getStudentId());
            if (s == null && studentMap.isEmpty()) {
                s = studentRepository.findById(kardex.getStudentId()).orElse(null);
            }
            if (s != null) {
                builder.studentName(s.getFirstName() + " " + s.getLastName());
                builder.enrollmentNumber(s.getEnrollmentNumber());
            }
        }

        if (kardex.getCourseId() != null) {
            Course c = courseMap.get(kardex.getCourseId());
            if (c == null && courseMap.isEmpty()) {
                c = courseRepository.findById(kardex.getCourseId()).orElse(null);
            }
            if (c != null) {
                builder.courseCode(c.getCourseCode());
                builder.courseName(c.getName());
                builder.courseCredits(c.getCredits());
            }
        }

        if (kardex.getAcademicSemesterId() != null) {
            AcademicSemester as = semesterMap.get(kardex.getAcademicSemesterId());
            if (as == null && semesterMap.isEmpty()) {
                as = academicSemesterRepository.findById(kardex.getAcademicSemesterId()).orElse(null);
            }
            if (as != null) {
                builder.academicSemesterName(as.getName());
            }
        }

        return builder.build();
    }
}
