package com.academic_system.service.registration;

import com.academic_system.dto.registration.*;
import com.academic_system.entity.postgres.*;
import com.academic_system.repository.postgres.*;
import com.academic_system.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class RegistrationService {

    private final RegistrationRequestRepository registrationRequestRepository;
    private final StudentRepository studentRepository;
    private final TeacherRepository teacherRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;

    private final SecureRandom random = new SecureRandom();

    private static final int OTP_EXPIRY_MINUTES = 30;

    public RegistrationRequestDTO initRegistration(RegistrationInitRequest request) {
        String curp = request.getCurp().toUpperCase();
        String email = request.getEmail().toLowerCase();

        if (registrationRequestRepository.existsByCurp(curp)) {
            throw new IllegalStateException("Ya existe una solicitud de registro con este CURP");
        }

        if (userRepository.existsByEmail(email)) {
            throw new IllegalStateException("El email ya está registrado en el sistema");
        }

        Optional<Student> studentOpt = studentRepository.findByCurpAndIsActiveTrueAndIsDeletedFalse(curp);
        Optional<Teacher> teacherOpt = teacherRepository.findByCurpAndIsActiveTrueAndIsDeletedFalse(curp);

        if (studentOpt.isEmpty() && teacherOpt.isEmpty()) {
            throw new IllegalStateException("No se encontró registro académico activo con este CURP");
        }

        Student student = studentOpt.orElse(null);
        Teacher teacher = teacherOpt.orElse(null);

        String otpCode = generateOtp();
        LocalDateTime otpExpires = LocalDateTime.now().plusMinutes(OTP_EXPIRY_MINUTES);

        RegistrationRequest regRequest = RegistrationRequest.builder()
                .curp(curp)
                .email(email)
                .studentId(student != null ? student.getId() : null)
                .teacherId(teacher != null ? teacher.getId() : null)
                .status(RegistrationRequest.RegistrationStatus.PENDING)
                .otpCode(otpCode)
                .otpExpiresAt(otpExpires)
                .build();

        regRequest = registrationRequestRepository.save(regRequest);

        try {
            emailService.sendEmail(email, "Código de verificación - Registro",
                    "Su código de verificación es: " + otpCode + "\n\nEste código expira en " + OTP_EXPIRY_MINUTES + " minutos.");
        } catch (Exception e) {
            log.error("Error enviando email de verificación", e);
        }

        return toDTO(regRequest, student, teacher);
    }

    @Transactional("postgresTransactionManager")
    public RegistrationRequestDTO verifyRegistration(RegistrationVerifyRequest request) {
        String curp = request.getCurp().toUpperCase();

        RegistrationRequest regRequest = registrationRequestRepository.findByCurp(curp)
                .orElseThrow(() -> new IllegalArgumentException("Solicitud no encontrada"));

        if (regRequest.getStatus() != RegistrationRequest.RegistrationStatus.PENDING) {
            throw new IllegalStateException("La solicitud ya fue procesada");
        }

        if (LocalDateTime.now().isAfter(regRequest.getOtpExpiresAt())) {
            regRequest.setStatus(RegistrationRequest.RegistrationStatus.EXPIRED);
            registrationRequestRepository.save(regRequest);
            throw new IllegalStateException("El código ha expirado. Solicite uno nuevo.");
        }

        if (!regRequest.getOtpCode().equals(request.getOtp())) {
            throw new IllegalArgumentException("Código incorrecto");
        }

        String tempPassword = generateTempPassword();
        String username = generateUsername(regRequest.getEmail());

        User user = new User();
        user.setUsername(username);
        user.setEmail(regRequest.getEmail());
        user.setPasswordHash(passwordEncoder.encode(tempPassword));
        user.setTempPassword(tempPassword);
        user.setIsVerified(false);
        user.setIsActive(true);
        user.setMustChangePassword(false);

        user = userRepository.save(user);

        Set<Role> roles = new HashSet<>();
        if (regRequest.getStudentId() != null) {
            Role studentRole = roleRepository.findByName("STUDENT").orElse(null);
            if (studentRole != null) {
                roles.add(studentRole);
            }
            Role teacherRole = roleRepository.findByName("TEACHER").orElse(null);
            if (teacherRole != null && roles.size() < 2) {
                roles.add(teacherRole);
            }
            user.setRoles(roles);
            user = userRepository.save(user);

            Student student = studentRepository.findById(regRequest.getStudentId()).orElse(null);
            if (student != null) {
                student.setUserId(user.getId());
                studentRepository.save(student);
            }
        } else if (regRequest.getTeacherId() != null) {
            Role teacherRole = roleRepository.findByName("TEACHER").orElse(null);
            if (teacherRole != null) {
                roles.add(teacherRole);
            }
            Role studentRole = roleRepository.findByName("STUDENT").orElse(null);
            if (studentRole != null && roles.size() < 2) {
                roles.add(studentRole);
            }
            user.setRoles(roles);
            user = userRepository.save(user);

            Teacher teacher = teacherRepository.findById(regRequest.getTeacherId()).orElse(null);
            if (teacher != null) {
                teacher.setUserId(user.getId());
                teacherRepository.save(teacher);
            }
        }

        regRequest.setStatus(RegistrationRequest.RegistrationStatus.APPROVED);
        regRequest.setProcessedAt(LocalDateTime.now());
        registrationRequestRepository.save(regRequest);

        Student student = regRequest.getStudentId() != null ? 
                studentRepository.findById(regRequest.getStudentId()).orElse(null) : null;
        Teacher teacher = regRequest.getTeacherId() != null ? 
                teacherRepository.findById(regRequest.getTeacherId()).orElse(null) : null;

        try {
            emailService.sendEmail(user.getEmail(), "Cuenta creada - Escuela Normal",
                    "Su cuenta ha sido creada y activada.\n\n" +
                    "Username: " + username + "\n" +
                    "Password temporal: " + tempPassword + "\n\n" +
                    "Puede iniciar sesión.");
        } catch (Exception e) {
            log.error("Error enviando email de credenciales", e);
        }

        return toDTO(regRequest, student, teacher);
    }

    public List<RegistrationRequestDTO> getPendingRequests() {
        return registrationRequestRepository.findByStatus(RegistrationRequest.RegistrationStatus.PENDING)
                .stream()
                .map(req -> {
                    Student student = req.getStudentId() != null ? 
                            studentRepository.findById(req.getStudentId()).orElse(null) : null;
                    Teacher teacher = req.getTeacherId() != null ? 
                            teacherRepository.findById(req.getTeacherId()).orElse(null) : null;
                    return toDTO(req, student, teacher);
                })
                .toList();
    }

    public List<RegistrationRequestDTO> getAllRequests() {
        return registrationRequestRepository.findAll()
                .stream()
                .map(req -> {
                    Student student = req.getStudentId() != null ? 
                            studentRepository.findById(req.getStudentId()).orElse(null) : null;
                    Teacher teacher = req.getTeacherId() != null ? 
                            teacherRepository.findById(req.getTeacherId()).orElse(null) : null;
                    return toDTO(req, student, teacher);
                })
                .toList();
    }

    public UserStatusDTO getUserStatus(String userId) {
        UUID userUuid = UUID.fromString(userId);
        User user = userRepository.findById(userUuid)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
        
        return UserStatusDTO.builder()
                .username(user.getUsername())
                .email(user.getEmail())
                .isActive(user.getIsActive())
                .isVerified(user.getIsVerified())
                .mustChangePassword(user.getMustChangePassword())
                .build();
    }

    private String generateOtp() {
        return String.format("%06d", random.nextInt(999999));
    }

    private String generateTempPassword() {
        String chars = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghjkmnpqrstuvwxyz23456789";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 12; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        return sb.toString();
    }

    private String generateUsername(String email) {
        String base = email.split("@")[0];
        String sanitized = base.replaceAll("[^a-zA-Z0-9]", "").toLowerCase();
        if (sanitized.length() < 3) {
            sanitized = "user" + System.currentTimeMillis() % 10000;
        }
        if (userRepository.existsByUsername(sanitized)) {
            return sanitized + random.nextInt(99);
        }
        return sanitized;
    }

    private RegistrationRequestDTO toDTO(RegistrationRequest req, Student student, Teacher teacher) {
        return RegistrationRequestDTO.builder()
                .id(req.getId())
                .curp(req.getCurp())
                .email(req.getEmail())
                .status(req.getStatus().name())
                .requestedAt(req.getRequestedAt())
                .processedAt(req.getProcessedAt())
                .rejectionReason(req.getRejectionReason())
                .studentId(req.getStudentId())
                .teacherId(req.getTeacherId())
                .studentName(student != null ? student.getFirstName() + " " + student.getLastName() : null)
                .teacherName(teacher != null ? teacher.getFirstName() + " " + teacher.getLastName() : null)
                .build();
    }
}