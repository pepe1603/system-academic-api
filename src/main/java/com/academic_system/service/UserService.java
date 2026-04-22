package com.academic_system.service;

import com.academic_system.dto.cpanel.CreateUserRequest;
import com.academic_system.dto.cpanel.UserDTO;
import com.academic_system.entity.postgres.Role;
import com.academic_system.entity.postgres.Student;
import com.academic_system.entity.postgres.Teacher;
import com.academic_system.entity.postgres.User;
import com.academic_system.repository.postgres.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final StudentRepository studentRepository;
    private final TeacherRepository teacherRepository;
    private final RoleRepository roleRepository;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;

    private final SecureRandom random = new SecureRandom();

    private static final Set<String> ROLES_REQUIRING_CURP = Set.of("STUDENT", "TEACHER");
    private static final Set<String> ROLES_WITHOUT_CURP = Set.of("ADMIN", "CONTROL_ESCOLAR", "DIRECTOR");

    public Page<UserDTO> getAllUsers(Pageable pageable) {
        Page<User> page = userRepository.findAll(pageable);
        return page.map(this::toDTO);
    }

    public UserDTO getUserById(String id) {
        UUID uuid = UUID.fromString(id);
        User user = userRepository.findById(uuid)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
        return toDTO(user);
    }

    @Transactional("postgresTransactionManager")
    public UserDTO createUser(CreateUserRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalStateException("El email ya está registrado");
        }

        validateRolesAndCurp(request);

        String tempPassword = generateTempPassword();
        String username = generateUsername(request.getEmail());

        UUID studentId = null;
        UUID teacherId = null;
        Student student = null;
        Teacher teacher = null;

        if (request.getCurp() != null && !request.getCurp().isEmpty()) {
            student = studentRepository.findByCurpAndIsActiveTrueAndIsDeletedFalse(request.getCurp().toUpperCase())
                    .orElse(null);
            if (student == null) {
                teacher = teacherRepository.findByCurpAndIsActiveTrueAndIsDeletedFalse(request.getCurp().toUpperCase())
                        .orElse(null);
            }
            if (student != null) {
                studentId = student.getId();
            } else if (teacher != null) {
                teacherId = teacher.getId();
            }
        }

        User user = new User();
        user.setUsername(username);
        user.setEmail(request.getEmail());
        user.setPasswordHash(passwordEncoder.encode(tempPassword));
        user.setTempPassword(tempPassword);
        user.setMustVerifyEmail(true);
        user.setIsVerified(false);
        user.setIsActive(true);
        user.setMustChangePassword(true);

        Set<Role> userRoles = new HashSet<>();
        if (request.getRoles() != null && !request.getRoles().isEmpty()) {
            for (String roleName : request.getRoles()) {
                roleRepository.findByName(roleName).ifPresent(userRoles::add);
            }
        } else {
            roleRepository.findByName("STUDENT").ifPresent(userRoles::add);
        }
        user.setRoles(userRoles);

        user = userRepository.save(user);

        if (studentId != null && student != null) {
            student.setUserId(user.getId());
            studentRepository.save(student);
        } else if (teacherId != null && teacher != null) {
            teacher.setUserId(user.getId());
            teacherRepository.save(teacher);
        }

        sendUserCredentialsEmail(user.getEmail(), username, tempPassword);

        return toDTO(user);
    }

    @Transactional("postgresTransactionManager")
    public UserDTO updateUser(String id, Boolean isActive, Set<String> roles) {
        UUID uuid = UUID.fromString(id);
        User user = userRepository.findById(uuid)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

        if (isActive != null) {
            user.setIsActive(isActive);
        }

        if (roles != null && !roles.isEmpty()) {
            user.getRoles().clear();
            for (String roleName : roles) {
                roleRepository.findByName(roleName).ifPresent(user.getRoles()::add);
            }
        }

        user = userRepository.save(user);
        return toDTO(user);
    }

    @Transactional("postgresTransactionManager")
    public void deleteUser(String id) {
        UUID uuid = UUID.fromString(id);
        User user = userRepository.findById(uuid)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

        user.setIsActive(false);
        user.setIsDeleted(true);
        userRepository.save(user);
    }

    private void validateRolesAndCurp(CreateUserRequest request) {
        List<String> roles = request.getRoles();
        String curp = request.getCurp();

        if (roles == null || roles.isEmpty()) {
            return;
        }

        boolean anyRoleRequiresCurp = roles.stream().anyMatch(ROLES_REQUIRING_CURP::contains);
        boolean anyRoleWithoutCurp = roles.stream().anyMatch(ROLES_WITHOUT_CURP::contains);

        if (anyRoleRequiresCurp && anyRoleWithoutCurp) {
            throw new IllegalStateException(
                    "No se puede asignar roles que requieren CURP con roles que no lo requieren");
        }

        if (anyRoleRequiresCurp && (curp == null || curp.isEmpty())) {
            throw new IllegalStateException(
                    "El CURP es requerido para los roles: " + ROLES_REQUIRING_CURP);
        }

        if (anyRoleWithoutCurp && curp != null && !curp.isEmpty()) {
            throw new IllegalStateException(
                    "Los roles " + ROLES_WITHOUT_CURP + " no requieren CURP");
        }

        if (anyRoleRequiresCurp) {
            boolean curpFound = studentRepository.findByCurpAndIsActiveTrueAndIsDeletedFalse(curp.toUpperCase()).isPresent() ||
                           teacherRepository.findByCurpAndIsActiveTrueAndIsDeletedFalse(curp.toUpperCase()).isPresent();
            if (!curpFound) {
                throw new IllegalStateException(
                        "El CURP no corresponde a un registro académico activo");
            }
        }
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
            sanitized = sanitized + random.nextInt(99);
        }
        return sanitized;
    }

    private void sendUserCredentialsEmail(String email, String username, String password) {
        try {
            emailService.sendEmail(email, "Cuenta creada - Escuela Normal",
                    "Su cuenta ha sido creada por el administrador.\n\n" +
                    "Username: " + username + "\n" +
                    "Password temporal: " + password + "\n\n" +
                    "Debe verificar su email.");
        } catch (Exception e) {
            log.error("Error enviando email de credenciales", e);
        }
    }

    private UserDTO toDTO(User user) {
        return UserDTO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .isActive(user.getIsActive())
                .isVerified(user.getIsVerified())
                .mustVerifyEmail(user.getMustVerifyEmail())
                .roles(user.getRoles().stream().map(Role::getName).collect(Collectors.toSet()))
                .createdAt(user.getCreatedAt())
                .build();
    }
}