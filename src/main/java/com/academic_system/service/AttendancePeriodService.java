package com.academic_system.service;

import com.academic_system.dto.cpanel.AttendancePeriodDTO;
import com.academic_system.dto.cpanel.CreateAttendancePeriodRequest;
import com.academic_system.dto.cpanel.UpdateAttendancePeriodRequest;
import com.academic_system.entity.postgres.AttendancePeriod;
import com.academic_system.entity.postgres.Course;
import com.academic_system.entity.postgres.Enrollment;
import com.academic_system.entity.postgres.Student;
import com.academic_system.exception.DuplicateResourceException;
import com.academic_system.exception.ResourceNotFoundException;
import com.academic_system.repository.postgres.AttendancePeriodRepository;
import com.academic_system.repository.postgres.CourseRepository;
import com.academic_system.repository.postgres.EnrollmentRepository;
import com.academic_system.repository.postgres.StudentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AttendancePeriodService {

    private final AttendancePeriodRepository attendancePeriodRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final StudentRepository studentRepository;
    private final CourseRepository courseRepository;

    @Transactional(readOnly = true)
    public Page<AttendancePeriodDTO> getAllAttendancePeriods(Pageable pageable) {
        Page<AttendancePeriod> page = attendancePeriodRepository.findAll(pageable);
        List<AttendancePeriod> periods = page.getContent();

        Set<UUID> enrollmentIds = periods.stream()
                .map(AttendancePeriod::getEnrollmentId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        Map<UUID, Enrollment> enrollmentMap = enrollmentRepository.findAllById(enrollmentIds).stream()
                .collect(Collectors.toMap(Enrollment::getId, e -> e));

        Set<UUID> studentIds = enrollmentMap.values().stream()
                .map(Enrollment::getStudentId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        Set<UUID> courseIds = enrollmentMap.values().stream()
                .map(Enrollment::getCourseId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        Map<UUID, Student> studentMap = studentRepository.findAllById(studentIds).stream()
                .collect(Collectors.toMap(Student::getId, s -> s));

        Map<UUID, Course> courseMap = courseRepository.findAllById(courseIds).stream()
                .collect(Collectors.toMap(Course::getId, c -> c));

        return page.map(period -> toDTO(period, enrollmentMap, studentMap, courseMap));
    }

    @Transactional(readOnly = true)
    public Optional<AttendancePeriodDTO> getAttendancePeriodById(String id) {
        return attendancePeriodRepository.findById(UUID.fromString(id))
                .map(period -> toDTO(period, Collections.emptyMap(), Collections.emptyMap(), Collections.emptyMap()));
    }

    @Transactional(readOnly = true)
    public Optional<AttendancePeriodDTO> getByEnrollmentAndSemester(String enrollmentId, String semesterId) {
        return attendancePeriodRepository
                .findByEnrollmentIdAndAcademicSemesterId(UUID.fromString(enrollmentId), UUID.fromString(semesterId))
                .map(period -> toDTO(period, Collections.emptyMap(), Collections.emptyMap(), Collections.emptyMap()));
    }

    @Transactional(readOnly = true)
    public Page<AttendancePeriodDTO> getByEnrollment(String enrollmentId, Pageable pageable) {
        Page<AttendancePeriod> page = attendancePeriodRepository
                .findByEnrollmentIdOrderByCreatedAtDesc(UUID.fromString(enrollmentId), pageable);
        List<AttendancePeriod> periods = page.getContent();

        Set<UUID> enrollmentIds = periods.stream()
                .map(AttendancePeriod::getEnrollmentId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        Map<UUID, Enrollment> enrollmentMap = enrollmentRepository.findAllById(enrollmentIds).stream()
                .collect(Collectors.toMap(Enrollment::getId, e -> e));

        Set<UUID> studentIds = enrollmentMap.values().stream()
                .map(Enrollment::getStudentId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        Set<UUID> courseIds = enrollmentMap.values().stream()
                .map(Enrollment::getCourseId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        Map<UUID, Student> studentMap = studentRepository.findAllById(studentIds).stream()
                .collect(Collectors.toMap(Student::getId, s -> s));

        Map<UUID, Course> courseMap = courseRepository.findAllById(courseIds).stream()
                .collect(Collectors.toMap(Course::getId, c -> c));

        return page.map(period -> toDTO(period, enrollmentMap, studentMap, courseMap));
    }

    @Transactional(readOnly = true)
    public Page<AttendancePeriodDTO> getByAcademicSemester(String semesterId, Pageable pageable) {
        Page<AttendancePeriod> page = attendancePeriodRepository
                .findByAcademicSemesterIdOrderByCreatedAtDesc(UUID.fromString(semesterId), pageable);
        List<AttendancePeriod> periods = page.getContent();

        Set<UUID> enrollmentIds = periods.stream()
                .map(AttendancePeriod::getEnrollmentId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        Map<UUID, Enrollment> enrollmentMap = enrollmentRepository.findAllById(enrollmentIds).stream()
                .collect(Collectors.toMap(Enrollment::getId, e -> e));

        Set<UUID> studentIds = enrollmentMap.values().stream()
                .map(Enrollment::getStudentId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        Set<UUID> courseIds = enrollmentMap.values().stream()
                .map(Enrollment::getCourseId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        Map<UUID, Student> studentMap = studentRepository.findAllById(studentIds).stream()
                .collect(Collectors.toMap(Student::getId, s -> s));

        Map<UUID, Course> courseMap = courseRepository.findAllById(courseIds).stream()
                .collect(Collectors.toMap(Course::getId, c -> c));

        return page.map(period -> toDTO(period, enrollmentMap, studentMap, courseMap));
    }

    @Transactional
    public AttendancePeriodDTO createAttendancePeriod(CreateAttendancePeriodRequest request) {
        if (!enrollmentRepository.existsById(request.getEnrollmentId())) {
            throw new ResourceNotFoundException("Inscripción no encontrada", "Enrollment", "id");
        }

        if (attendancePeriodRepository.existsByEnrollmentIdAndAcademicSemesterId(
                request.getEnrollmentId(), request.getAcademicSemesterId())) {
            throw new DuplicateResourceException("Ya existe un período de asistencia para esta inscripción y semestre", "AttendancePeriod");
        }

        AttendancePeriod period = AttendancePeriod.builder()
                .enrollmentId(request.getEnrollmentId())
                .academicSemesterId(request.getAcademicSemesterId())
                .totalClasses(request.getTotalClasses() != null ? request.getTotalClasses() : 0)
                .totalPresent(request.getTotalPresent() != null ? request.getTotalPresent() : 0)
                .totalAbsent(request.getTotalAbsent() != null ? request.getTotalAbsent() : 0)
                .totalJustified(request.getTotalJustified() != null ? request.getTotalJustified() : 0)
                .totalLate(request.getTotalLate() != null ? request.getTotalLate() : 0)
                .observations(request.getObservations())
                .build();

        period = attendancePeriodRepository.save(period);
        log.info("Created attendance period: {} for enrollment {}", period.getId(), period.getEnrollmentId());
        return toDTO(period, Collections.emptyMap(), Collections.emptyMap(), Collections.emptyMap());
    }

    @Transactional
    public AttendancePeriodDTO updateAttendancePeriod(String id, UpdateAttendancePeriodRequest request) {
        AttendancePeriod period = attendancePeriodRepository.findById(UUID.fromString(id))
                .orElseThrow(() -> new ResourceNotFoundException("Período de asistencia no encontrado", "AttendancePeriod", "id"));

        if (request.getTotalClasses() != null) period.setTotalClasses(request.getTotalClasses());
        if (request.getTotalPresent() != null) period.setTotalPresent(request.getTotalPresent());
        if (request.getTotalAbsent() != null) period.setTotalAbsent(request.getTotalAbsent());
        if (request.getTotalJustified() != null) period.setTotalJustified(request.getTotalJustified());
        if (request.getTotalLate() != null) period.setTotalLate(request.getTotalLate());
        if (request.getObservations() != null) period.setObservations(request.getObservations());

        period = attendancePeriodRepository.save(period);
        log.info("Updated attendance period: {}", period.getId());
        return toDTO(period, Collections.emptyMap(), Collections.emptyMap(), Collections.emptyMap());
    }

    @Transactional
    public void deleteAttendancePeriod(String id) {
        AttendancePeriod period = attendancePeriodRepository.findById(UUID.fromString(id))
                .orElseThrow(() -> new ResourceNotFoundException("Período de asistencia no encontrado", "AttendancePeriod", "id"));
        attendancePeriodRepository.delete(period);
        log.info("Deleted attendance period: {}", id);
    }

    private AttendancePeriodDTO toDTO(AttendancePeriod period, Map<UUID, Enrollment> enrollmentMap,
                                       Map<UUID, Student> studentMap, Map<UUID, Course> courseMap) {
        AttendancePeriodDTO.AttendancePeriodDTOBuilder builder = AttendancePeriodDTO.builder()
                .id(period.getId())
                .enrollmentId(period.getEnrollmentId())
                .academicSemesterId(period.getAcademicSemesterId())
                .totalClasses(period.getTotalClasses())
                .totalPresent(period.getTotalPresent())
                .totalAbsent(period.getTotalAbsent())
                .totalJustified(period.getTotalJustified())
                .totalLate(period.getTotalLate())
                .attendancePercentage(period.getAttendancePercentage())
                .attendanceStatus(period.getAttendanceStatus())
                .observations(period.getObservations())
                .createdAt(period.getCreatedAt())
                .updatedAt(period.getUpdatedAt());

        if (period.getEnrollmentId() != null) {
            Enrollment enrollment = enrollmentMap.isEmpty()
                    ? enrollmentRepository.findById(period.getEnrollmentId()).orElse(null)
                    : enrollmentMap.get(period.getEnrollmentId());

            if (enrollment != null) {
                if (enrollment.getStudentId() != null) {
                    Student student = studentMap.isEmpty()
                            ? studentRepository.findById(enrollment.getStudentId()).orElse(null)
                            : studentMap.get(enrollment.getStudentId());

                    if (student != null) {
                        builder.studentName(student.getFirstName() + " " + student.getLastName());
                        builder.enrollmentNumber(student.getEnrollmentNumber());
                    }
                }
                if (enrollment.getCourseId() != null) {
                    Course course = courseMap.isEmpty()
                            ? courseRepository.findById(enrollment.getCourseId()).orElse(null)
                            : courseMap.get(enrollment.getCourseId());

                    if (course != null) {
                        builder.courseCode(course.getCourseCode());
                        builder.courseName(course.getName());
                    }
                }
            }
        }

        return builder.build();
    }
}
