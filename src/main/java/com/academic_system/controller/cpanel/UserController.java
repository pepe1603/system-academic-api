package com.academic_system.controller.cpanel;

import com.academic_system.dto.auth.ApiResponse;
import com.academic_system.dto.registration.CreateUserRequest;
import com.academic_system.entity.postgres.Role;
import com.academic_system.entity.postgres.Student;
import com.academic_system.entity.postgres.Teacher;
import com.academic_system.entity.postgres.User;
import com.academic_system.repository.postgres.*;
import com.academic_system.service.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.security.SecureRandom;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/cpanel/users")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class UserController {

    private final UserRepository userRepository;
    private final StudentRepository studentRepository;
    private final TeacherRepository teacherRepository;
    private final RoleRepository roleRepository;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;

    private final SecureRandom random = new SecureRandom();

    @GetMapping
    public ResponseEntity<ApiResponse<Page<UserDTO>>> getAllUsers(Pageable pageable) {
        Page<User> page = userRepository.findAll(pageable);
        Page<UserDTO> dtoPage = page.map(this::toUserDTO);
        return ResponseEntity.ok(ApiResponse.success(dtoPage));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<UserDTO>> getUser(@PathVariable String id) {
        UUID uuid = UUID.fromString(id);
        User user = userRepository.findById(uuid)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
        return ResponseEntity.ok(ApiResponse.success(toUserDTO(user)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<UserDTO>> createUser(@RequestBody CreateUserRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalStateException("El email ya está registrado");
        }

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

        Set<Role> userRoles = new java.util.HashSet<>();
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

        try {
            emailService.sendEmail(user.getEmail(), "Cuenta creada - Escuela Normal",
                    "Su cuenta ha sido creada por el administrador.\n\n" +
                    "Username: " + username + "\n" +
                    "Password temporal: " + tempPassword + "\n\n" +
                    "Debe verificar su email.");
        } catch (Exception e) {
            // Log error but don't fail the request
        }

        return ResponseEntity.ok(ApiResponse.success("Usuario creado", toUserDTO(user)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<UserDTO>> updateUser(@PathVariable String id, @RequestBody UpdateUserRequest request) {
        UUID uuid = UUID.fromString(id);
        User user = userRepository.findById(uuid)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

        if (request.getIsActive() != null) {
            user.setIsActive(request.getIsActive());
        }

        if (request.getRoles() != null && !request.getRoles().isEmpty()) {
            user.getRoles().clear();
            for (String roleName : request.getRoles()) {
                roleRepository.findByName(roleName).ifPresent(user.getRoles()::add);
            }
        }

        user = userRepository.save(user);
        return ResponseEntity.ok(ApiResponse.success("Usuario actualizado", toUserDTO(user)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable String id) {
        UUID uuid = UUID.fromString(id);
        User user = userRepository.findById(uuid)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

        user.setIsActive(false);
        user.setIsDeleted(true);
        userRepository.save(user);

        return ResponseEntity.ok(ApiResponse.success("Usuario eliminado", null));
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

    private UserDTO toUserDTO(User user) {
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

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class UserDTO {
        private UUID id;
        private String username;
        private String email;
        private Boolean isActive;
        private Boolean isVerified;
        private Boolean mustVerifyEmail;
        private Set<String> roles;
        private java.time.LocalDateTime createdAt;
    }

    @lombok.Data
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class UpdateUserRequest {
        private Boolean isActive;
        private Set<String> roles;
    }
}