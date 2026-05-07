package com.academic_system.service;

import com.academic_system.dto.cpanel.CreateStudentRequest;
import com.academic_system.dto.cpanel.StudentDTO;
import com.academic_system.dto.cpanel.UpdateStudentRequest;
import com.academic_system.entity.postgres.Generation;
import com.academic_system.entity.postgres.Student;
import com.academic_system.repository.postgres.GenerationRepository;
import com.academic_system.repository.postgres.StudentRepository;
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
public class StudentService {

    private final StudentRepository studentRepository;
    private final GenerationRepository generationRepository;

    @Transactional(readOnly = true)
    public Page<StudentDTO> getAllStudents(Pageable pageable) {
        return studentRepository.findAllByIsDeletedFalse(pageable)
                .map(this::toDTO);
    }

    @Transactional(readOnly = true)
    public Optional<StudentDTO> getStudentById(String id) {
        return studentRepository.findById(UUID.fromString(id))
                .filter(s -> !Boolean.TRUE.equals(s.getIsDeleted()))
                .map(this::toDTO);
    }

    @Transactional(readOnly = true)
    public List<StudentDTO> getDeletedStudents(Pageable pageable) {
        return studentRepository.findAllByIsDeletedTrue(pageable)
                .map(this::toDTO)
                .getContent();
    }

    @Transactional
    public StudentDTO createStudent(CreateStudentRequest request) {
        if (studentRepository.existsByCurpAndIsDeletedFalse(request.getCurp().toUpperCase())) {
            throw new IllegalArgumentException("Ya existe un estudiante con ese CURP");
        }
        if (studentRepository.existsByEnrollmentNumberAndIsDeletedFalse(request.getEnrollmentNumber().toUpperCase())) {
            throw new IllegalArgumentException("Ya existe un estudiante con esa matrícula");
        }

        Generation generation = generationRepository.findById(request.getGenerationId())
                .orElseThrow(() -> new IllegalArgumentException("Generación no encontrada"));

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
        return toDTO(student);
    }

    @Transactional
    public StudentDTO updateStudent(String id, UpdateStudentRequest request) {
        Student student = studentRepository.findById(UUID.fromString(id))
                .orElseThrow(() -> new IllegalArgumentException("Estudiante no encontrado"));

        if (request.getCurp() != null) {
            String newCurp = request.getCurp().toUpperCase();
            if (!student.getCurp().equals(newCurp) &&
                    studentRepository.existsByCurpAndIsDeletedFalseAndIdNot(newCurp, student.getId())) {
                throw new IllegalArgumentException("Ya existe un estudiante con ese CURP");
            }
            student.setCurp(newCurp);
        }
        if (request.getEnrollmentNumber() != null) {
            String newEnrollment = request.getEnrollmentNumber().toUpperCase();
            if (!student.getEnrollmentNumber().equals(newEnrollment) &&
                    studentRepository.existsByEnrollmentNumberAndIsDeletedFalseAndIdNot(newEnrollment, student.getId())) {
                throw new IllegalArgumentException("Ya existe un estudiante con esa matrícula");
            }
            student.setEnrollmentNumber(newEnrollment);
        }
        if (request.getFirstName() != null) student.setFirstName(request.getFirstName());
        if (request.getLastName() != null) student.setLastName(request.getLastName());
        if (request.getGenerationId() != null) {
            generationRepository.findById(request.getGenerationId())
                    .orElseThrow(() -> new IllegalArgumentException("Generación no encontrada"));
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
        return toDTO(student);
    }

    @Transactional
    public void deleteStudent(String id) {
        Student student = studentRepository.findById(UUID.fromString(id))
                .orElseThrow(() -> new IllegalArgumentException("Estudiante no encontrado"));
        student.setIsDeleted(true);
        studentRepository.save(student);
        log.info("Deleted student: {}", id);
    }

    private StudentDTO toDTO(Student student) {
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
            generationRepository.findById(student.getGenerationId()).ifPresent(g ->
                    builder.generationName(g.getName()));
        }

        return builder.build();
    }
}
