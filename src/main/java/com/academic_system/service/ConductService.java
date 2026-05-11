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
public class ConductService {

    private final ConductRepository conductRepository;
    private final ConductIncidentRepository conductIncidentRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final StudentRepository studentRepository;
    private final CourseRepository courseRepository;
    private final AcademicSemesterRepository academicSemesterRepository;

    private static final List<String> VALID_INCIDENT_TYPES = List.of(
            "WARNING", "CONGRATULATION", "CALL_ATTENTION", "SUSPENSION", "OTHER");

    private static final List<String> VALID_SEVERITIES = List.of("MINOR", "MODERATE", "SERIOUS");

    @Transactional(readOnly = true)
    public Page<ConductDTO> getAllConductRecords(Pageable pageable) {
        return conductRepository.findAllByIsDeletedFalse(pageable)
                .map(this::toDTO);
    }

    @Transactional(readOnly = true)
    public Optional<ConductDTO> getConductById(String id) {
        return conductRepository.findById(UUID.fromString(id))
                .filter(c -> !Boolean.TRUE.equals(c.getIsDeleted()))
                .map(this::toDTO);
    }

    @Transactional(readOnly = true)
    public List<ConductDTO> getConductByEnrollment(String enrollmentId) {
        return conductRepository.findByEnrollmentIdAndIsDeletedFalse(UUID.fromString(enrollmentId))
                .stream()
                .map(this::toDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ConductDTO> getConductBySemester(String semesterId) {
        return conductRepository.findByAcademicSemesterIdAndIsDeletedFalse(UUID.fromString(semesterId))
                .stream()
                .map(this::toDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ConductDTO> getDeletedConductRecords(Pageable pageable) {
        return conductRepository.findAllByIsDeletedTrue(pageable)
                .map(this::toDTO)
                .getContent();
    }

    @Transactional
    public ConductDTO createConduct(CreateConductRequest request) {
        Enrollment enrollment = enrollmentRepository.findById(request.getEnrollmentId())
                .orElseThrow(() -> new IllegalArgumentException("Inscripción no encontrada"));

        AcademicSemester academicSemester = academicSemesterRepository.findById(request.getAcademicSemesterId())
                .orElseThrow(() -> new IllegalArgumentException("Semestre académico no encontrado"));

        if (conductRepository.existsByEnrollmentIdAndAcademicSemesterIdAndIsDeletedFalse(
                request.getEnrollmentId(), request.getAcademicSemesterId())) {
            throw new IllegalArgumentException("Ya existe un registro de conducta para esta inscripción y semestre");
        }

        Conduct conduct = Conduct.builder()
                .enrollmentId(enrollment.getId())
                .academicSemesterId(academicSemester.getId())
                .grade(request.getGrade())
                .observations(request.getObservations())
                .warnings(request.getWarnings() != null ? request.getWarnings() : 0)
                .congratulations(request.getCongratulations() != null ? request.getCongratulations() : 0)
                .recordedBy(request.getRecordedBy())
                .build();

        conduct = conductRepository.save(conduct);
        log.info("Created conduct record: {} for enrollment {}", conduct.getId(), enrollment.getId());
        return toDTO(conduct);
    }

    @Transactional
    public ConductDTO updateConduct(String id, UpdateConductRequest request) {
        Conduct conduct = conductRepository.findById(UUID.fromString(id))
                .orElseThrow(() -> new IllegalArgumentException("Registro de conducta no encontrado"));

        if (request.getGrade() != null) conduct.setGrade(request.getGrade());
        if (request.getObservations() != null) conduct.setObservations(request.getObservations());
        if (request.getWarnings() != null) conduct.setWarnings(request.getWarnings());
        if (request.getCongratulations() != null) conduct.setCongratulations(request.getCongratulations());
        if (request.getRecordedBy() != null) conduct.setRecordedBy(request.getRecordedBy());

        conduct = conductRepository.save(conduct);
        log.info("Updated conduct record: {}", conduct.getId());
        return toDTO(conduct);
    }

    @Transactional
    public void deleteConduct(String id) {
        Conduct conduct = conductRepository.findById(UUID.fromString(id))
                .orElseThrow(() -> new IllegalArgumentException("Registro de conducta no encontrado"));
        conduct.setIsDeleted(true);
        conductRepository.save(conduct);
        log.info("Deleted conduct record: {}", id);
    }

    @Transactional(readOnly = true)
    public List<ConductIncidentDTO> getIncidentsByEnrollment(String enrollmentId) {
        return conductIncidentRepository.findByEnrollmentIdAndIsDeletedFalse(UUID.fromString(enrollmentId))
                .stream()
                .map(this::toIncidentDTO)
                .toList();
    }

    @Transactional
    public ConductIncidentDTO createIncident(CreateConductIncidentRequest request) {
        enrollmentRepository.findById(request.getEnrollmentId())
                .orElseThrow(() -> new IllegalArgumentException("Inscripción no encontrada"));

        String type = request.getIncidentType().toUpperCase();
        if (!VALID_INCIDENT_TYPES.contains(type)) {
            throw new IllegalArgumentException("Tipo de incidente inválido. Valores: WARNING, CONGRATULATION, CALL_ATTENTION, SUSPENSION, OTHER");
        }

        String severity = request.getSeverity() != null ? request.getSeverity().toUpperCase() : "MINOR";
        if (!VALID_SEVERITIES.contains(severity)) {
            throw new IllegalArgumentException("Severidad inválida. Valores: MINOR, MODERATE, SERIOUS");
        }

        ConductIncident incident = ConductIncident.builder()
                .enrollmentId(request.getEnrollmentId())
                .incidentType(type)
                .description(request.getDescription())
                .incidentDate(request.getIncidentDate())
                .severity(severity)
                .actionsTaken(request.getActionsTaken())
                .attentionDate(request.getAttentionDate())
                .recordedBy(request.getRecordedBy())
                .build();

        incident = conductIncidentRepository.save(incident);
        log.info("Created conduct incident: {} for enrollment {}", incident.getId(), request.getEnrollmentId());
        return toIncidentDTO(incident);
    }

    @Transactional
    public ConductIncidentDTO updateIncident(String id, UpdateConductIncidentRequest request) {
        ConductIncident incident = conductIncidentRepository.findById(UUID.fromString(id))
                .orElseThrow(() -> new IllegalArgumentException("Incidente no encontrado"));

        if (request.getIncidentType() != null) {
            String newType = request.getIncidentType().toUpperCase();
            if (!VALID_INCIDENT_TYPES.contains(newType)) {
                throw new IllegalArgumentException("Tipo de incidente inválido");
            }
            incident.setIncidentType(newType);
        }
        if (request.getDescription() != null) incident.setDescription(request.getDescription());
        if (request.getIncidentDate() != null) incident.setIncidentDate(request.getIncidentDate());
        if (request.getSeverity() != null) {
            String newSeverity = request.getSeverity().toUpperCase();
            if (!VALID_SEVERITIES.contains(newSeverity)) {
                throw new IllegalArgumentException("Severidad inválida");
            }
            incident.setSeverity(newSeverity);
        }
        if (request.getActionsTaken() != null) incident.setActionsTaken(request.getActionsTaken());
        if (request.getAttentionDate() != null) incident.setAttentionDate(request.getAttentionDate());

        incident = conductIncidentRepository.save(incident);
        log.info("Updated conduct incident: {}", incident.getId());
        return toIncidentDTO(incident);
    }

    @Transactional
    public void deleteIncident(String id) {
        ConductIncident incident = conductIncidentRepository.findById(UUID.fromString(id))
                .orElseThrow(() -> new IllegalArgumentException("Incidente no encontrado"));
        incident.setIsDeleted(true);
        conductIncidentRepository.save(incident);
        log.info("Deleted conduct incident: {}", id);
    }

    @Transactional(readOnly = true)
    public List<ConductIncidentDTO> getDeletedIncidents() {
        return conductIncidentRepository.findAll()
                .stream()
                .filter(i -> Boolean.TRUE.equals(i.getIsDeleted()))
                .map(this::toIncidentDTO)
                .toList();
    }

    private ConductDTO toDTO(Conduct conduct) {
        ConductDTO.ConductDTOBuilder builder = ConductDTO.builder()
                .id(conduct.getId())
                .grade(conduct.getGrade())
                .observations(conduct.getObservations())
                .warnings(conduct.getWarnings())
                .congratulations(conduct.getCongratulations())
                .isDeleted(conduct.getIsDeleted())
                .registrationDate(conduct.getRegistrationDate())
                .enrollmentId(conduct.getEnrollmentId())
                .academicSemesterId(conduct.getAcademicSemesterId());

        if (conduct.getEnrollmentId() != null) {
            enrollmentRepository.findById(conduct.getEnrollmentId()).ifPresent(e -> {
                builder.courseId(e.getCourseId());
                builder.studentId(e.getStudentId());
                if (e.getStudentId() != null) {
                    studentRepository.findById(e.getStudentId()).ifPresent(s -> {
                        builder.studentName(s.getFirstName() + " " + s.getLastName());
                        builder.enrollmentNumber(s.getEnrollmentNumber());
                    });
                }
                if (e.getCourseId() != null) {
                    courseRepository.findById(e.getCourseId()).ifPresent(c -> {
                        builder.courseCode(c.getCourseCode());
                        builder.courseName(c.getName());
                    });
                }
            });
        }

        if (conduct.getAcademicSemesterId() != null) {
            academicSemesterRepository.findById(conduct.getAcademicSemesterId()).ifPresent(as ->
                    builder.academicSemesterName(as.getName()));
        }

        return builder.build();
    }

    private ConductIncidentDTO toIncidentDTO(ConductIncident incident) {
        ConductIncidentDTO.ConductIncidentDTOBuilder builder = ConductIncidentDTO.builder()
                .id(incident.getId())
                .enrollmentId(incident.getEnrollmentId())
                .incidentType(incident.getIncidentType())
                .description(incident.getDescription())
                .incidentDate(incident.getIncidentDate())
                .severity(incident.getSeverity())
                .actionsTaken(incident.getActionsTaken())
                .attentionDate(incident.getAttentionDate())
                .isDeleted(incident.getIsDeleted())
                .createdAt(incident.getCreatedAt());

        if (incident.getEnrollmentId() != null) {
            enrollmentRepository.findById(incident.getEnrollmentId()).ifPresent(e -> {
                if (e.getStudentId() != null) {
                    studentRepository.findById(e.getStudentId()).ifPresent(s -> {
                        builder.studentName(s.getFirstName() + " " + s.getLastName());
                        builder.enrollmentNumber(s.getEnrollmentNumber());
                    });
                }
            });
        }

        return builder.build();
    }
}
