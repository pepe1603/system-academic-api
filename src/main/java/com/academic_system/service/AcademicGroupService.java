package com.academic_system.service;

import com.academic_system.dto.cpanel.AcademicGroupDTO;
import com.academic_system.dto.cpanel.CreateAcademicGroupRequest;
import com.academic_system.dto.cpanel.UpdateAcademicGroupRequest;
import com.academic_system.entity.postgres.AcademicGroup;
import com.academic_system.entity.postgres.AcademicSemester;
import com.academic_system.entity.postgres.Course;
import com.academic_system.entity.postgres.Teacher;
import com.academic_system.repository.postgres.AcademicGroupRepository;
import com.academic_system.repository.postgres.AcademicSemesterRepository;
import com.academic_system.repository.postgres.CourseRepository;
import com.academic_system.repository.postgres.TeacherRepository;
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
public class AcademicGroupService {

    private final AcademicGroupRepository academicGroupRepository;
    private final AcademicSemesterRepository academicSemesterRepository;
    private final CourseRepository courseRepository;
    private final TeacherRepository teacherRepository;

    @Transactional(readOnly = true)
    public Page<AcademicGroupDTO> getAllAcademicGroups(Pageable pageable) {
        return academicGroupRepository.findAllByIsDeletedFalse(pageable)
                .map(this::toDTO);
    }

    @Transactional(readOnly = true)
    public Optional<AcademicGroupDTO> getAcademicGroupById(String id) {
        return academicGroupRepository.findById(UUID.fromString(id))
                .filter(g -> !Boolean.TRUE.equals(g.getIsDeleted()))
                .map(this::toDTO);
    }

    @Transactional(readOnly = true)
    public List<AcademicGroupDTO> getDeletedAcademicGroups(Pageable pageable) {
        return academicGroupRepository.findAllByIsDeletedTrue(pageable)
                .map(this::toDTO)
                .getContent();
    }

    @Transactional
    public AcademicGroupDTO createAcademicGroup(CreateAcademicGroupRequest request) {
        if (academicGroupRepository.existsByNameAndAcademicSemesterIdAndCourseIdAndIsDeletedFalse(
                request.getName(), request.getAcademicSemesterId(), request.getCourseId())) {
            throw new IllegalArgumentException("Ya existe un grupo con ese nombre en el semestre y curso seleccionados");
        }

        AcademicSemester academicSemester = academicSemesterRepository.findById(request.getAcademicSemesterId())
                .orElseThrow(() -> new IllegalArgumentException("Semestre académico no encontrado"));

        Course course = courseRepository.findById(request.getCourseId())
                .orElseThrow(() -> new IllegalArgumentException("Curso no encontrado"));

        AcademicGroup group = AcademicGroup.builder()
                .name(request.getName())
                .academicSemesterId(academicSemester.getId())
                .courseId(course.getId())
                .teacherId(request.getTeacherId())
                .capacity(request.getCapacity() != null ? request.getCapacity() : 30)
                .build();

        group = academicGroupRepository.save(group);
        log.info("Created academic group: {} ({})", group.getName(), group.getId());
        return toDTO(group);
    }

    @Transactional
    public AcademicGroupDTO updateAcademicGroup(String id, UpdateAcademicGroupRequest request) {
        AcademicGroup group = academicGroupRepository.findById(UUID.fromString(id))
                .orElseThrow(() -> new IllegalArgumentException("Grupo académico no encontrado"));

        if (request.getName() != null || request.getAcademicSemesterId() != null || request.getCourseId() != null) {
            String newName = request.getName() != null ? request.getName() : group.getName();
            UUID newSemesterId = request.getAcademicSemesterId() != null ? request.getAcademicSemesterId() : group.getAcademicSemesterId();
            UUID newCourseId = request.getCourseId() != null ? request.getCourseId() : group.getCourseId();

            if (!group.getName().equals(newName) || !group.getAcademicSemesterId().equals(newSemesterId) || !group.getCourseId().equals(newCourseId)) {
                if (academicGroupRepository.existsByNameAndAcademicSemesterIdAndCourseIdAndIsDeletedFalseAndIdNot(
                        newName, newSemesterId, newCourseId, group.getId())) {
                    throw new IllegalArgumentException("Ya existe un grupo con ese nombre en el semestre y curso seleccionados");
                }
            }
        }

        if (request.getName() != null) group.setName(request.getName());
        if (request.getAcademicSemesterId() != null) {
            academicSemesterRepository.findById(request.getAcademicSemesterId())
                    .orElseThrow(() -> new IllegalArgumentException("Semestre académico no encontrado"));
            group.setAcademicSemesterId(request.getAcademicSemesterId());
        }
        if (request.getCourseId() != null) {
            courseRepository.findById(request.getCourseId())
                    .orElseThrow(() -> new IllegalArgumentException("Curso no encontrado"));
            group.setCourseId(request.getCourseId());
        }
        if (request.getTeacherId() != null) {
            teacherRepository.findById(request.getTeacherId())
                    .orElseThrow(() -> new IllegalArgumentException("Profesor no encontrado"));
            group.setTeacherId(request.getTeacherId());
        }
        if (request.getCapacity() != null) group.setCapacity(request.getCapacity());
        if (request.getIsActive() != null) group.setIsActive(request.getIsActive());

        group = academicGroupRepository.save(group);
        log.info("Updated academic group: {} ({})", group.getName(), group.getId());
        return toDTO(group);
    }

    @Transactional
    public void deleteAcademicGroup(String id) {
        AcademicGroup group = academicGroupRepository.findById(UUID.fromString(id))
                .orElseThrow(() -> new IllegalArgumentException("Grupo académico no encontrado"));
        group.setIsDeleted(true);
        academicGroupRepository.save(group);
        log.info("Deleted academic group: {}", id);
    }

    private AcademicGroupDTO toDTO(AcademicGroup group) {
        AcademicGroupDTO.AcademicGroupDTOBuilder builder = AcademicGroupDTO.builder()
                .id(group.getId())
                .name(group.getName())
                .capacity(group.getCapacity())
                .isActive(group.getIsActive())
                .isDeleted(group.getIsDeleted())
                .createdAt(group.getCreatedAt())
                .academicSemesterId(group.getAcademicSemesterId())
                .courseId(group.getCourseId())
                .teacherId(group.getTeacherId());

        if (group.getAcademicSemesterId() != null) {
            academicSemesterRepository.findById(group.getAcademicSemesterId()).ifPresent(as ->
                    builder.academicSemesterName(as.getName()));
        }

        if (group.getCourseId() != null) {
            courseRepository.findById(group.getCourseId()).ifPresent(c -> {
                builder.courseCode(c.getCourseCode());
                builder.courseName(c.getName());
            });
        }

        if (group.getTeacherId() != null) {
            teacherRepository.findById(group.getTeacherId()).ifPresent(t ->
                    builder.teacherFullName(t.getFirstName() + " " + t.getLastName()));
        }

        return builder.build();
    }
}
