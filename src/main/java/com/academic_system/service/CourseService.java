package com.academic_system.service;

import com.academic_system.dto.cpanel.CourseDTO;
import com.academic_system.dto.cpanel.CreateCourseRequest;
import com.academic_system.dto.cpanel.UpdateCourseRequest;
import com.academic_system.entity.postgres.Course;
import com.academic_system.entity.postgres.Semester;
import com.academic_system.entity.postgres.StudyPlan;
import com.academic_system.repository.postgres.CourseRepository;
import com.academic_system.repository.postgres.SemesterRepository;
import com.academic_system.repository.postgres.StudyPlanRepository;
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
public class CourseService {

    private final CourseRepository courseRepository;
    private final StudyPlanRepository studyPlanRepository;
    private final SemesterRepository semesterRepository;

    @Transactional(readOnly = true)
    public Page<CourseDTO> getAllCourses(Pageable pageable) {
        return courseRepository.findAllByIsDeletedFalse(pageable)
                .map(this::toDTO);
    }

    @Transactional(readOnly = true)
    public Optional<CourseDTO> getCourseById(String id) {
        return courseRepository.findById(UUID.fromString(id))
                .filter(c -> !Boolean.TRUE.equals(c.getIsDeleted()))
                .map(this::toDTO);
    }

    @Transactional(readOnly = true)
    public List<CourseDTO> getDeletedCourses(Pageable pageable) {
        return courseRepository.findAllByIsDeletedTrue(pageable)
                .map(this::toDTO)
                .getContent();
    }

    @Transactional
    public CourseDTO createCourse(CreateCourseRequest request) {
        if (courseRepository.existsByCourseCodeAndIsDeletedFalse(request.getCourseCode().toUpperCase())) {
            throw new IllegalArgumentException("Ya existe un curso con ese código");
        }

        StudyPlan studyPlan = studyPlanRepository.findById(request.getStudyPlanId())
                .orElseThrow(() -> new IllegalArgumentException("Plan de estudio no encontrado"));

        Semester semester = semesterRepository.findById(request.getSemesterId())
                .orElseThrow(() -> new IllegalArgumentException("Semestre no encontrado"));

        Course course = Course.builder()
                .studyPlanId(studyPlan.getId())
                .semesterId(semester.getId())
                .courseCode(request.getCourseCode().toUpperCase())
                .name(request.getName())
                .credits(request.getCredits())
                .hoursTheory(request.getHoursTheory() != null ? request.getHoursTheory() : 0)
                .hoursPractice(request.getHoursPractice() != null ? request.getHoursPractice() : 0)
                .description(request.getDescription())
                .isMandatory(request.getIsMandatory() != null ? request.getIsMandatory() : true)
                .isActive(request.getIsActive() != null ? request.getIsActive() : true)
                .build();

        course = courseRepository.save(course);
        log.info("Created course: {} ({})", course.getCourseCode(), course.getId());
        return toDTO(course);
    }

    @Transactional
    public CourseDTO updateCourse(String id, UpdateCourseRequest request) {
        Course course = courseRepository.findById(UUID.fromString(id))
                .orElseThrow(() -> new IllegalArgumentException("Curso no encontrado"));

        if (request.getCourseCode() != null) {
            String newCode = request.getCourseCode().toUpperCase();
            if (!course.getCourseCode().equals(newCode) &&
                    courseRepository.existsByCourseCodeAndIsDeletedFalseAndIdNot(newCode, course.getId())) {
                throw new IllegalArgumentException("Ya existe un curso con ese código");
            }
            course.setCourseCode(newCode);
        }
        if (request.getName() != null) course.setName(request.getName());
        if (request.getCredits() != null) course.setCredits(request.getCredits());
        if (request.getHoursTheory() != null) course.setHoursTheory(request.getHoursTheory());
        if (request.getHoursPractice() != null) course.setHoursPractice(request.getHoursPractice());
        if (request.getDescription() != null) course.setDescription(request.getDescription());
        if (request.getIsMandatory() != null) course.setIsMandatory(request.getIsMandatory());
        if (request.getIsActive() != null) course.setIsActive(request.getIsActive());

        if (request.getStudyPlanId() != null) {
            StudyPlan studyPlan = studyPlanRepository.findById(request.getStudyPlanId())
                    .orElseThrow(() -> new IllegalArgumentException("Plan de estudio no encontrado"));
            course.setStudyPlanId(studyPlan.getId());
        }
        if (request.getSemesterId() != null) {
            Semester semester = semesterRepository.findById(request.getSemesterId())
                    .orElseThrow(() -> new IllegalArgumentException("Semestre no encontrado"));
            course.setSemesterId(semester.getId());
        }

        course = courseRepository.save(course);
        log.info("Updated course: {} ({})", course.getCourseCode(), course.getId());
        return toDTO(course);
    }

    @Transactional
    public void deleteCourse(String id) {
        Course course = courseRepository.findById(UUID.fromString(id))
                .orElseThrow(() -> new IllegalArgumentException("Curso no encontrado"));
        course.setIsDeleted(true);
        courseRepository.save(course);
        log.info("Deleted course: {}", id);
    }

    private CourseDTO toDTO(Course course) {
        CourseDTO.CourseDTOBuilder builder = CourseDTO.builder()
                .id(course.getId())
                .courseCode(course.getCourseCode())
                .name(course.getName())
                .credits(course.getCredits())
                .hoursTheory(course.getHoursTheory())
                .hoursPractice(course.getHoursPractice())
                .description(course.getDescription())
                .isMandatory(course.getIsMandatory())
                .isActive(course.getIsActive())
                .isDeleted(course.getIsDeleted())
                .createdAt(course.getCreatedAt())
                .studyPlanId(course.getStudyPlanId())
                .semesterId(course.getSemesterId());

        if (course.getStudyPlanId() != null) {
            studyPlanRepository.findById(course.getStudyPlanId()).ifPresent(sp -> {
                builder.studyPlanCode(sp.getCode());
                builder.studyPlanName(sp.getName());
            });
        }

        if (course.getSemesterId() != null) {
            semesterRepository.findById(course.getSemesterId()).ifPresent(s -> {
                builder.semesterName(s.getName());
                builder.semesterNumber(s.getSemesterNumber());
            });
        }

        return builder.build();
    }
}
