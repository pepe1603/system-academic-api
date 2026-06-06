package com.academic_system.service;

import com.academic_system.dto.cpanel.CreateTeacherRequest;
import com.academic_system.dto.cpanel.TeacherDTO;
import com.academic_system.dto.cpanel.UpdateTeacherRequest;
import com.academic_system.entity.postgres.Teacher;
import com.academic_system.exception.DuplicateResourceException;
import com.academic_system.exception.ResourceNotFoundException;
import com.academic_system.repository.postgres.TeacherRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class TeacherService {

    private final TeacherRepository teacherRepository;

    @Transactional(readOnly = true)
    public Page<TeacherDTO> getAllTeachers(Pageable pageable) {
        return teacherRepository.findAllByIsDeletedFalse(pageable)
                .map(this::toDTO);
    }

    @Transactional(readOnly = true)
    public Optional<TeacherDTO> getTeacherById(String id) {
        return teacherRepository.findById(UUID.fromString(id))
                .filter(t -> !Boolean.TRUE.equals(t.getIsDeleted()))
                .map(this::toDTO);
    }

    @Transactional(readOnly = true)
    public List<TeacherDTO> getDeletedTeachers(Pageable pageable) {
        return teacherRepository.findAllByIsDeletedTrue(pageable)
                .map(this::toDTO)
                .getContent();
    }

    @Transactional
    public TeacherDTO createTeacher(CreateTeacherRequest request) {
        if (request.getRfc() != null && teacherRepository.existsByRfc(request.getRfc())) {
            throw new DuplicateResourceException("El RFC ya está registrado", "Teacher", "rfc");
        }
        if (request.getCurp() != null && teacherRepository.existsByCurp(request.getCurp())) {
            throw new DuplicateResourceException("El CURP ya está registrado", "Teacher", "curp");
        }
        if (request.getEmployeeNumber() != null && teacherRepository.existsByEmployeeNumber(request.getEmployeeNumber())) {
            throw new DuplicateResourceException("El número de empleado ya está registrado", "Teacher", "employeeNumber");
        }

        Teacher teacher = Teacher.builder()
                .userId(request.getUserId())
                .employeeNumber(request.getEmployeeNumber())
                .rfc(request.getRfc())
                .curp(request.getCurp())
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .institutionalEmail(request.getInstitutionalEmail())
                .phone(request.getPhone())
                .build();

        teacher = teacherRepository.save(teacher);
        log.info("Created teacher: {} {} ({})", teacher.getFirstName(), teacher.getLastName(), teacher.getId());
        return toDTO(teacher);
    }

    @Transactional
    public TeacherDTO updateTeacher(String id, UpdateTeacherRequest request) {
        Teacher teacher = teacherRepository.findById(UUID.fromString(id))
                .orElseThrow(() -> new ResourceNotFoundException("Docente no encontrado", "Teacher", "id"));

        if (request.getRfc() != null && !request.getRfc().equals(teacher.getRfc())
                && teacherRepository.existsByRfc(request.getRfc())) {
            throw new DuplicateResourceException("El RFC ya está registrado por otro docente", "Teacher", "rfc");
        }
        if (request.getCurp() != null && !request.getCurp().equals(teacher.getCurp())
                && teacherRepository.existsByCurp(request.getCurp())) {
            throw new DuplicateResourceException("El CURP ya está registrado por otro docente", "Teacher", "curp");
        }
        if (request.getEmployeeNumber() != null && !request.getEmployeeNumber().equals(teacher.getEmployeeNumber())
                && teacherRepository.existsByEmployeeNumber(request.getEmployeeNumber())) {
            throw new DuplicateResourceException("El número de empleado ya está registrado por otro docente", "Teacher", "employeeNumber");
        }

        if (request.getUserId() != null) teacher.setUserId(request.getUserId());
        if (request.getEmployeeNumber() != null) teacher.setEmployeeNumber(request.getEmployeeNumber());
        if (request.getRfc() != null) teacher.setRfc(request.getRfc());
        if (request.getCurp() != null) teacher.setCurp(request.getCurp());
        if (request.getFirstName() != null) teacher.setFirstName(request.getFirstName());
        if (request.getLastName() != null) teacher.setLastName(request.getLastName());
        if (request.getInstitutionalEmail() != null) teacher.setInstitutionalEmail(request.getInstitutionalEmail());
        if (request.getSecondaryEmail() != null) teacher.setSecondaryEmail(request.getSecondaryEmail());
        if (request.getPhone() != null) teacher.setPhone(request.getPhone());
        if (request.getSecondaryPhone() != null) teacher.setSecondaryPhone(request.getSecondaryPhone());
        if (request.getIsActive() != null) teacher.setIsActive(request.getIsActive());

        teacher.setUpdatedAt(LocalDateTime.now());
        teacher = teacherRepository.save(teacher);
        log.info("Updated teacher: {}", teacher.getId());
        return toDTO(teacher);
    }

    @Transactional
    public void deleteTeacher(String id) {
        Teacher teacher = teacherRepository.findById(UUID.fromString(id))
                .orElseThrow(() -> new ResourceNotFoundException("Docente no encontrado", "Teacher", "id"));
        teacher.setIsDeleted(true);
        teacher.setUpdatedAt(LocalDateTime.now());
        teacherRepository.save(teacher);
        log.info("Deleted teacher: {}", id);
    }

    private TeacherDTO toDTO(Teacher teacher) {
        return TeacherDTO.builder()
                .id(teacher.getId())
                .userId(teacher.getUserId())
                .employeeNumber(teacher.getEmployeeNumber())
                .rfc(teacher.getRfc())
                .curp(teacher.getCurp())
                .firstName(teacher.getFirstName())
                .lastName(teacher.getLastName())
                .institutionalEmail(teacher.getInstitutionalEmail())
                .secondaryEmail(teacher.getSecondaryEmail())
                .phone(teacher.getPhone())
                .secondaryPhone(teacher.getSecondaryPhone())
                .isActive(teacher.getIsActive())
                .isDeleted(teacher.getIsDeleted())
                .createdAt(teacher.getCreatedAt())
                .updatedAt(teacher.getUpdatedAt())
                .build();
    }
}
