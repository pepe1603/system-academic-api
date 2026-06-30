package com.academic_system.service;

import com.academic_system.dto.cpanel.*;
import com.academic_system.entity.postgres.*;
import com.academic_system.exception.DuplicateResourceException;
import com.academic_system.exception.ResourceNotFoundException;
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
public class RetakeExamService {

    private final RetakeExamRepository retakeExamRepository;
    private final StudentRepository studentRepository;
    private final CourseRepository courseRepository;
    private final AcademicSemesterRepository academicSemesterRepository;

    @Transactional(readOnly = true)
    public Page<RetakeExamDTO> getAllRetakeExams(Pageable pageable) {
        return retakeExamRepository.findAllByIsDeletedFalse(pageable)
                .map(this::toDTO);
    }

    @Transactional(readOnly = true)
    public Optional<RetakeExamDTO> getRetakeExamById(String id) {
        return retakeExamRepository.findById(UUID.fromString(id))
                .filter(r -> !Boolean.TRUE.equals(r.getIsDeleted()))
                .map(this::toDTO);
    }

    @Transactional(readOnly = true)
    public List<RetakeExamDTO> getRetakeExamsByStudent(String studentId) {
        return retakeExamRepository.findByStudentIdAndIsDeletedFalse(UUID.fromString(studentId))
                .stream()
                .map(this::toDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<RetakeExamDTO> getRetakeExamsByCourse(String courseId) {
        return retakeExamRepository.findByCourseIdAndIsDeletedFalse(UUID.fromString(courseId))
                .stream()
                .map(this::toDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<RetakeExamDTO> getRetakeExamsBySemester(String semesterId) {
        return retakeExamRepository.findByAcademicSemesterIdAndIsDeletedFalse(UUID.fromString(semesterId))
                .stream()
                .map(this::toDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<RetakeExamDTO> getDeletedRetakeExams(Pageable pageable) {
        return retakeExamRepository.findAllByIsDeletedTrue(pageable)
                .map(this::toDTO)
                .getContent();
    }

    @Transactional
    public RetakeExamDTO createRetakeExam(CreateRetakeExamRequest request) {
        Student student = studentRepository.findById(request.getStudentId())
                .orElseThrow(() -> new ResourceNotFoundException("Estudiante no encontrado", "Student", "id"));

        Course course = courseRepository.findById(request.getCourseId())
                .orElseThrow(() -> new ResourceNotFoundException("Curso no encontrado", "Course", "id"));

        AcademicSemester academicSemester = academicSemesterRepository.findById(request.getAcademicSemesterId())
                .orElseThrow(() -> new ResourceNotFoundException("Semestre académico no encontrado", "AcademicSemester", "id"));

        if (retakeExamRepository.existsByStudentIdAndCourseIdAndAcademicSemesterIdAndIsDeletedFalse(
                request.getStudentId(), request.getCourseId(), request.getAcademicSemesterId())) {
            throw new DuplicateResourceException("Ya existe un registro de retake para este estudiante, curso y semestre", "RetakeExam");
        }

        if (request.getOriginSemesterId() != null) {
            academicSemesterRepository.findById(request.getOriginSemesterId())
                    .orElseThrow(() -> new ResourceNotFoundException("Semestre de origen no encontrado", "AcademicSemester", "id"));
        }

        RetakeExam retakeExam = RetakeExam.builder()
                .studentId(student.getId())
                .courseId(course.getId())
                .academicSemesterId(academicSemester.getId())
                .originSemesterId(request.getOriginSemesterId())
                .previousAverage(request.getPreviousAverage())
                .build();

        retakeExam = retakeExamRepository.save(retakeExam);
        log.info("Created retake exam: {} - {} ({})", student.getEnrollmentNumber(), course.getCourseCode(), retakeExam.getId());
        return toDTO(retakeExam);
    }

    @Transactional
    public RetakeExamDTO updateRetakeExam(String id, UpdateRetakeExamRequest request) {
        RetakeExam retakeExam = retakeExamRepository.findById(UUID.fromString(id))
                .orElseThrow(() -> new ResourceNotFoundException("Registro de retake no encontrado", "RetakeExam", "id"));

        if (request.getOriginSemesterId() != null) {
            academicSemesterRepository.findById(request.getOriginSemesterId())
                    .orElseThrow(() -> new ResourceNotFoundException("Semestre de origen no encontrado", "AcademicSemester", "id"));
            retakeExam.setOriginSemesterId(request.getOriginSemesterId());
        }
        if (request.getPreviousAverage() != null) retakeExam.setPreviousAverage(request.getPreviousAverage());
        if (request.getStatus() != null) retakeExam.setStatus(request.getStatus());

        retakeExam = retakeExamRepository.save(retakeExam);
        log.info("Updated retake exam: {}", retakeExam.getId());
        return toDTO(retakeExam);
    }

    @Transactional
    public void deleteRetakeExam(String id) {
        RetakeExam retakeExam = retakeExamRepository.findById(UUID.fromString(id))
                .orElseThrow(() -> new ResourceNotFoundException("Registro de retake no encontrado", "RetakeExam", "id"));
        retakeExam.setIsDeleted(true);
        retakeExamRepository.save(retakeExam);
        log.info("Deleted retake exam: {}", id);
    }

    private RetakeExamDTO toDTO(RetakeExam retakeExam) {
        RetakeExamDTO.RetakeExamDTOBuilder builder = RetakeExamDTO.builder()
                .id(retakeExam.getId())
                .previousAverage(retakeExam.getPreviousAverage())
                .status(retakeExam.getStatus())
                .isDeleted(retakeExam.getIsDeleted())
                .createdAt(retakeExam.getCreatedAt())
                .studentId(retakeExam.getStudentId())
                .courseId(retakeExam.getCourseId())
                .academicSemesterId(retakeExam.getAcademicSemesterId())
                .originSemesterId(retakeExam.getOriginSemesterId());

        if (retakeExam.getStudentId() != null) {
            studentRepository.findById(retakeExam.getStudentId()).ifPresent(s -> {
                builder.studentName(s.getFirstName() + " " + s.getLastName());
                builder.enrollmentNumber(s.getEnrollmentNumber());
            });
        }

        if (retakeExam.getCourseId() != null) {
            courseRepository.findById(retakeExam.getCourseId()).ifPresent(c -> {
                builder.courseCode(c.getCourseCode());
                builder.courseName(c.getName());
            });
        }

        if (retakeExam.getAcademicSemesterId() != null) {
            academicSemesterRepository.findById(retakeExam.getAcademicSemesterId()).ifPresent(as ->
                    builder.academicSemesterName(as.getName()));
        }

        if (retakeExam.getOriginSemesterId() != null) {
            academicSemesterRepository.findById(retakeExam.getOriginSemesterId()).ifPresent(os ->
                    builder.originSemesterName(os.getName()));
        }

        return builder.build();
    }
}
