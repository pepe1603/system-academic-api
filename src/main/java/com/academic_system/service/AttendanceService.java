package com.academic_system.service;

import com.academic_system.dto.cpanel.AttendanceDTO;
import com.academic_system.dto.cpanel.CreateAttendanceRequest;
import com.academic_system.dto.cpanel.UpdateAttendanceRequest;
import com.academic_system.entity.postgres.Attendance;
import com.academic_system.entity.postgres.Course;
import com.academic_system.entity.postgres.Enrollment;
import com.academic_system.entity.postgres.Student;
import com.academic_system.exception.DuplicateResourceException;
import com.academic_system.exception.ResourceNotFoundException;
import com.academic_system.exception.ValidationException;
import com.academic_system.repository.postgres.AttendanceRepository;
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
public class AttendanceService {

    private final AttendanceRepository attendanceRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final StudentRepository studentRepository;
    private final CourseRepository courseRepository;

    private static final List<String> VALID_STATUSES = List.of("PRESENT", "ABSENT", "JUSTIFIED", "LATE");

    @Transactional(readOnly = true)
    public Page<AttendanceDTO> getAllAttendances(Pageable pageable) {
        Page<Attendance> attendancePage = attendanceRepository.findAllByIsDeletedFalse(pageable);
        List<Attendance> attendances = attendancePage.getContent();

        Set<UUID> enrollmentIds = attendances.stream()
                .map(Attendance::getEnrollmentId)
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

        return attendancePage.map(a -> toDTO(a, enrollmentMap, studentMap, courseMap));
    }

    @Transactional(readOnly = true)
    public Optional<AttendanceDTO> getAttendanceById(String id) {
        return attendanceRepository.findById(UUID.fromString(id))
                .filter(a -> !Boolean.TRUE.equals(a.getIsDeleted()))
                .map(a -> toDTO(a, Collections.emptyMap(), Collections.emptyMap(), Collections.emptyMap()));
    }

    @Transactional(readOnly = true)
    public List<AttendanceDTO> getAttendancesByEnrollment(String enrollmentId) {
        List<Attendance> attendances = attendanceRepository.findByEnrollmentIdAndIsDeletedFalse(UUID.fromString(enrollmentId));

        Set<UUID> enrollmentIds = attendances.stream()
                .map(Attendance::getEnrollmentId)
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

        return attendances.stream()
                .map(a -> toDTO(a, enrollmentMap, studentMap, courseMap))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<AttendanceDTO> getDeletedAttendances(Pageable pageable) {
        Page<Attendance> attendancePage = attendanceRepository.findAllByIsDeletedTrue(pageable);
        List<Attendance> attendances = attendancePage.getContent();

        Set<UUID> enrollmentIds = attendances.stream()
                .map(Attendance::getEnrollmentId)
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

        return attendancePage
                .map(a -> toDTO(a, enrollmentMap, studentMap, courseMap))
                .getContent();
    }

    @Transactional
    public AttendanceDTO createAttendance(CreateAttendanceRequest request) {
        Enrollment enrollment = enrollmentRepository.findById(request.getEnrollmentId())
                .orElseThrow(() -> new ResourceNotFoundException("Inscripción no encontrada", "Enrollment", "id"));

        if (attendanceRepository.existsByEnrollmentIdAndAttendanceDateAndIsDeletedFalse(
                request.getEnrollmentId(), request.getAttendanceDate())) {
            throw new DuplicateResourceException("Ya existe un registro de asistencia para esta fecha", "Attendance");
        }

        String status = request.getStatus() != null ? request.getStatus().toUpperCase() : "PRESENT";
        if (!VALID_STATUSES.contains(status)) {
            throw new ValidationException("Estado inválido. Valores permitidos: PRESENT, ABSENT, JUSTIFIED, LATE", "Attendance", "status");
        }

        Attendance attendance = Attendance.builder()
                .enrollmentId(enrollment.getId())
                .attendanceDate(request.getAttendanceDate())
                .status(status)
                .classTime(request.getClassTime())
                .subjectCode(request.getSubjectCode())
                .observations(request.getObservations())
                .build();

        attendance = attendanceRepository.save(attendance);
        log.info("Created attendance: {} for enrollment {}", attendance.getAttendanceDate(), enrollment.getId());
        return toDTO(attendance, Collections.emptyMap(), Collections.emptyMap(), Collections.emptyMap());
    }

    @Transactional
    public AttendanceDTO updateAttendance(String id, UpdateAttendanceRequest request) {
        Attendance attendance = attendanceRepository.findById(UUID.fromString(id))
                .orElseThrow(() -> new ResourceNotFoundException("Registro de asistencia no encontrado", "Attendance", "id"));

        if (request.getStatus() != null) {
            String newStatus = request.getStatus().toUpperCase();
            if (!VALID_STATUSES.contains(newStatus)) {
                throw new ValidationException("Estado inválido. Valores permitidos: PRESENT, ABSENT, JUSTIFIED, LATE", "Attendance", "status");
            }
            attendance.setStatus(newStatus);
        }
        if (request.getClassTime() != null) attendance.setClassTime(request.getClassTime());
        if (request.getSubjectCode() != null) attendance.setSubjectCode(request.getSubjectCode());
        if (request.getObservations() != null) attendance.setObservations(request.getObservations());
        if (request.getJustifiedBy() != null) attendance.setJustifiedBy(request.getJustifiedBy());
        if (request.getJustificationDate() != null) attendance.setJustificationDate(request.getJustificationDate());

        attendance = attendanceRepository.save(attendance);
        log.info("Updated attendance: {}", attendance.getId());
        return toDTO(attendance, Collections.emptyMap(), Collections.emptyMap(), Collections.emptyMap());
    }

    @Transactional
    public void deleteAttendance(String id) {
        Attendance attendance = attendanceRepository.findById(UUID.fromString(id))
                .orElseThrow(() -> new ResourceNotFoundException("Registro de asistencia no encontrado", "Attendance", "id"));
        attendance.setIsDeleted(true);
        attendanceRepository.save(attendance);
        log.info("Deleted attendance: {}", id);
    }

    private AttendanceDTO toDTO(Attendance attendance, Map<UUID, Enrollment> enrollmentMap,
                                Map<UUID, Student> studentMap, Map<UUID, Course> courseMap) {
        AttendanceDTO.AttendanceDTOBuilder builder = AttendanceDTO.builder()
                .id(attendance.getId())
                .attendanceDate(attendance.getAttendanceDate())
                .status(attendance.getStatus())
                .classTime(attendance.getClassTime())
                .subjectCode(attendance.getSubjectCode())
                .observations(attendance.getObservations())
                .justificationDate(attendance.getJustificationDate())
                .recordedAt(attendance.getRecordedAt())
                .isDeleted(attendance.getIsDeleted())
                .enrollmentId(attendance.getEnrollmentId());

        if (attendance.getEnrollmentId() != null) {
            Enrollment enrollment = enrollmentMap.isEmpty()
                    ? enrollmentRepository.findById(attendance.getEnrollmentId()).orElse(null)
                    : enrollmentMap.get(attendance.getEnrollmentId());

            if (enrollment != null) {
                builder.courseId(enrollment.getCourseId());
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
