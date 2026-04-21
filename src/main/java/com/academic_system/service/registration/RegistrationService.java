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
    private final EmailVerificationRepository emailVerificationRepository;
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

        User user = User.builder()
                .username(username)
                .email(regRequest.getEmail())
                .passwordHash(passwordEncoder.encode(tempPassword))
                .tempPassword(tempPassword)
                .mustVerifyEmail(true)
                .isVerified(false)
                .isActive(true)
                .mustChangePassword(true)
                .build();

        user = userRepository.save(user);

        Role studentRole = roleRepository.findByName("STUDENT").orElse(null);
        if (studentRole != null) {
            user.getRoles().add(studentRole);
            user = userRepository.save(user);
        }

        Student student = null;
        Teacher teacher = null;

        if (regRequest.getStudentId() != null) {
            student = studentRepository.findById(regRequest.getStudentId()).orElse(null);
            if (student != null) {
                student.setUserId(user.getId());
                student = studentRepository.save(student);
            }
        } else if (regRequest.getTeacherId() != null) {
            teacher = teacherRepository.findById(regRequest.getTeacherId()).orElse(null);
            if (teacher != null) {
                teacher.setUserId(user.getId());
                teacher = teacherRepository.save(teacher);
            }
        }

        String emailOtp = generateOtp();
        EmailVerification emailVerif = EmailVerification.builder()
                .userId(user.getId())
                .verificationCode(emailOtp)
                .expiresAt(LocalDateTime.now().plusMinutes(OTP_EXPIRY_MINUTES))
                .isVerified(false)
                .build();
        emailVerificationRepository.save(emailVerif);

        regRequest.setStatus(RegistrationRequest.RegistrationStatus.APPROVED);
        regRequest.setProcessedAt(LocalDateTime.now());
        registrationRequestRepository.save(regRequest);

        try {
            emailService.sendEmail(user.getEmail(), "Cuenta creada - Verificación de email",
                    "Su cuenta ha sido creada.\n\n" +
                    "Username: " + username + "\n" +
                    "Password temporal: " + tempPassword + "\n\n" +
                    "Debe verificar su email antes de iniciar sesión.\n" +
                    "Código de verificación: " + emailOtp);
        } catch (Exception e) {
            log.error("Error enviando email de verificación de cuenta", e);
        }

        return toDTO(regRequest, student, teacher);
    }

    @Transactional("postgresTransactionManager")
    public void verifyEmail(String userId, String code) {
        UUID userUuid = UUID.fromString(userId);
        
        User user = userRepository.findById(userUuid)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

        EmailVerification emailVerif = emailVerificationRepository.findByUserIdAndIsVerifiedFalse(userUuid)
                .orElseThrow(() -> new IllegalArgumentException("No hay verificación pendiente"));

        if (LocalDateTime.now().isAfter(emailVerif.getExpiresAt())) {
            throw new IllegalStateException("El código ha expirado");
        }

        if (!emailVerif.getVerificationCode().equals(code)) {
            throw new IllegalArgumentException("Código incorrecto");
        }

        emailVerif.setIsVerified(true);
        emailVerif.setVerifiedAt(LocalDateTime.now());
        emailVerificationRepository.save(emailVerif);

        user.setIsVerified(true);
        user.setVerifiedAt(LocalDateTime.now());
        user.setMustVerifyEmail(false);
        userRepository.save(user);
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

    public void resendEmailVerificationOtp(String userId) {
        UUID userUuid = UUID.fromString(userId);
        
        User user = userRepository.findById(userUuid)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
        
        if (Boolean.TRUE.equals(user.getIsVerified())) {
            throw new IllegalStateException("El email ya está verificado");
        }
        
        EmailVerification existing = emailVerificationRepository.findByUserIdAndIsVerifiedFalse(userUuid)
                .orElse(null);
        
        if (existing != null) {
            emailVerificationRepository.delete(existing);
        }
        
        String newOtp = generateOtp();
        EmailVerification newVerif = EmailVerification.builder()
                .userId(user.getId())
                .verificationCode(newOtp)
                .expiresAt(LocalDateTime.now().plusMinutes(30))
                .isVerified(false)
                .build();
        emailVerificationRepository.save(newVerif);
        
        try {
            emailService.sendEmail(user.getEmail(), "Código de verificación - Reenvío",
                    "Su código de verificación es: " + newOtp + "\n\nEste código expira en 30 minutos.");
        } catch (Exception e) {
            log.error("Error enviando email de verificación", e);
        }
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