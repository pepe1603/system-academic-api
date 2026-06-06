package com.academic_system.service;

import com.academic_system.dto.cpanel.CreateStudentRequest;
import com.academic_system.dto.cpanel.StudentDTO;
import com.academic_system.dto.cpanel.UpdateStudentRequest;
import com.academic_system.entity.postgres.Generation;
import com.academic_system.entity.postgres.Student;
import com.academic_system.exception.DuplicateResourceException;
import com.academic_system.exception.ResourceNotFoundException;
import com.academic_system.repository.postgres.GenerationRepository;
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
public class StudentService {

    private final StudentRepository studentRepository;
    private final GenerationRepository generationRepository;

    @Transactional(readOnly = true)
    public Page<StudentDTO> getAllStudents(Pageable pageable) {
        Page<Student> students = studentRepository.findAllByIsDeletedFalse(pageable);

        Set<UUID> generationIds = students.getContent().stream()
                .map(Student::getGenerationId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        Map<UUID, Generation> generationMap = generationRepository.findAllById(generationIds).stream()
                .collect(Collectors.toMap(Generation::getId, g -> g));

        return students.map(s -> toDTO(s, generationMap));
    }

    @Transactional(readOnly = true)
    public Optional<StudentDTO> getStudentById(String id) {
        return studentRepository.findById(UUID.fromString(id))
                .filter(s -> !Boolean.TRUE.equals(s.getIsDeleted()))
                .map(s -> toDTO(s, Collections.emptyMap()));
    }

    @Transactional(readOnly = true)
    public List<StudentDTO> getDeletedStudents(Pageable pageable) {
        Page<Student> students = studentRepository.findAllByIsDeletedTrue(pageable);

        Set<UUID> generationIds = students.getContent().stream()
                .map(Student::getGenerationId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        Map<UUID, Generation> generationMap = generationRepository.findAllById(generationIds).stream()
                .collect(Collectors.toMap(Generation::getId, g -> g));

        return students.map(s -> toDTO(s, generationMap)).getContent();
    }

    @Transactional
    public StudentDTO createStudent(CreateStudentRequest request) {
        if (studentRepository.existsByCurpAndIsDeletedFalse(request.getCurp().toUpperCase())) {
            throw new DuplicateResourceException("Ya existe un estudiante con ese CURP", "Student", "curp");
        }
        if (studentRepository.existsByEnrollmentNumberAndIsDeletedFalse(request.getEnrollmentNumber().toUpperCase())) {
            throw new DuplicateResourceException("Ya existe un estudiante con esa matrícula", "Student", "enrollmentNumber");
        }

        Generation generation = generationRepository.findById(request.getGenerationId())
                .orElseThrow(() -> new ResourceNotFoundException("Generación no encontrada", "Generation", "id"));

        Student student = Student.builder()
                .curp(request.getCurp().toUpperCase())
                .enrollmentNumber(request.getEnrollmentNumber().toUpperCase())
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .generationId(generation.getId())
                .userId(request.getUserId())
                .institutionalEmail(request.getInstitutionalEmail())
                .phone(request.getPhone())
                .birthDate(request.getBirthDate())
                .gender(request.getGender())
                .enrollmentDate(request.getEnrollmentDate())
                .build();

        student = studentRepository.save(student);
        log.info("Created student: {} ({})", student.getEnrollmentNumber(), student.getId());
        return toDTO(student, Collections.emptyMap());
    }

    @Transactional
    public StudentDTO updateStudent(String id, UpdateStudentRequest request) {
        Student student = studentRepository.findById(UUID.fromString(id))
                .orElseThrow(() -> new ResourceNotFoundException("Estudiante no encontrado", "Student", "id"));

        if (request.getCurp() != null) {
            String newCurp = request.getCurp().toUpperCase();
            if (!student.getCurp().equals(newCurp) &&
                    studentRepository.existsByCurpAndIsDeletedFalseAndIdNot(newCurp, student.getId())) {
                throw new DuplicateResourceException("Ya existe un estudiante con ese CURP", "Student", "curp");
            }
            student.setCurp(newCurp);
        }
        if (request.getEnrollmentNumber() != null) {
            String newEnrollment = request.getEnrollmentNumber().toUpperCase();
            if (!student.getEnrollmentNumber().equals(newEnrollment) &&
                    studentRepository.existsByEnrollmentNumberAndIsDeletedFalseAndIdNot(newEnrollment, student.getId())) {
                throw new DuplicateResourceException("Ya existe un estudiante con esa matrícula", "Student", "enrollmentNumber");
            }
            student.setEnrollmentNumber(newEnrollment);
        }
        if (request.getFirstName() != null) student.setFirstName(request.getFirstName());
        if (request.getLastName() != null) student.setLastName(request.getLastName());
        if (request.getGenerationId() != null) {
            generationRepository.findById(request.getGenerationId())
                    .orElseThrow(() -> new ResourceNotFoundException("Generación no encontrada", "Generation", "id"));
            student.setGenerationId(request.getGenerationId());
        }
        if (request.getUserId() != null) student.setUserId(request.getUserId());
        if (request.getInstitutionalEmail() != null) student.setInstitutionalEmail(request.getInstitutionalEmail());
        if (request.getPhone() != null) student.setPhone(request.getPhone());
        if (request.getBirthDate() != null) student.setBirthDate(request.getBirthDate());
        if (request.getGender() != null) student.setGender(request.getGender());
        if (request.getEnrollmentDate() != null) student.setEnrollmentDate(request.getEnrollmentDate());
        if (request.getIsActive() != null) student.setIsActive(request.getIsActive());

        student = studentRepository.save(student);
        log.info("Updated student: {} ({})", student.getEnrollmentNumber(), student.getId());
        return toDTO(student, Collections.emptyMap());
    }

    @Transactional
    public void deleteStudent(String id) {
        Student student = studentRepository.findById(UUID.fromString(id))
                .orElseThrow(() -> new ResourceNotFoundException("Estudiante no encontrado", "Student", "id"));
        student.setIsDeleted(true);
        studentRepository.save(student);
        log.info("Deleted student: {}", id);
    }

    private StudentDTO toDTO(Student student, Map<UUID, Generation> generationMap) {
        StudentDTO.StudentDTOBuilder builder = StudentDTO.builder()
                .id(student.getId())
                .userId(student.getUserId())
                .enrollmentNumber(student.getEnrollmentNumber())
                .curp(student.getCurp())
                .firstName(student.getFirstName())
                .lastName(student.getLastName())
                .institutionalEmail(student.getInstitutionalEmail())
                .phone(student.getPhone())
                .birthDate(student.getBirthDate())
                .gender(student.getGender())
                .enrollmentDate(student.getEnrollmentDate())
                .generationId(student.getGenerationId())
                .isActive(student.getIsActive())
                .isDeleted(student.getIsDeleted())
                .createdAt(student.getCreatedAt() != null ? student.getCreatedAt().toLocalDate() : null);

        if (student.getGenerationId() != null) {
            Generation generation = generationMap.get(student.getGenerationId());
            if (generation != null) {
                builder.generationName(generation.getName());
            } else if (generationMap.isEmpty()) {
                generationRepository.findById(student.getGenerationId()).ifPresent(g ->
                        builder.generationName(g.getName()));
            }
        }

        return builder.build();
    }
}
