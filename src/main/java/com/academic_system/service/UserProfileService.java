package com.academic_system.service;

import com.academic_system.dto.cpanel.EnrichedProfileDTO;
import com.academic_system.dto.cpanel.UpdateProfileRequest;
import com.academic_system.dto.cpanel.UserProfileDTO;
import com.academic_system.entity.postgres.Student;
import com.academic_system.entity.postgres.Teacher;
import com.academic_system.entity.postgres.User;
import com.academic_system.entity.postgres.UserProfile;
import com.academic_system.repository.postgres.StudentRepository;
import com.academic_system.repository.postgres.TeacherRepository;
import com.academic_system.repository.postgres.UserProfileRepository;
import com.academic_system.repository.postgres.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserProfileService {

    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;
    private final StudentRepository studentRepository;
    private final TeacherRepository teacherRepository;

    @Transactional(readOnly = true)
    public Optional<EnrichedProfileDTO> getEnrichedProfileByUserId(String userId) {
        Optional<User> userOpt = userRepository.findById(java.util.UUID.fromString(userId));
        if (userOpt.isEmpty()) {
            return Optional.empty();
        }

        User user = userOpt.get();
        Set<String> roles = user.getRoles().stream()
                .map(role -> role.getName())
                .collect(Collectors.toSet());

        Optional<UserProfile> profileOpt = userProfileRepository.findByUserId(user.getId());
        UserProfileDTO profileDTO = profileOpt.map(this::toDTO).orElse(null);

        EnrichedProfileDTO enrichedProfile;
        if (profileDTO != null) {
            enrichedProfile = EnrichedProfileDTO.fromUserProfile(profileDTO, roles);
        } else {
            enrichedProfile = EnrichedProfileDTO.builder()
                    .roles(roles)
                    .build();
        }

        // Enrich with academic data based on roles and CURP
        if (profileDTO != null && profileDTO.getCurp() != null) {
            if (roles.contains("STUDENT")) {
                studentRepository.findByCurpAndIsDeletedFalse(profileDTO.getCurp())
                        .ifPresent(enrichedProfile::enrichWithStudent);
            }
            if (roles.contains("TEACHER")) {
                teacherRepository.findByCurpAndIsDeletedFalse(profileDTO.getCurp())
                        .ifPresent(enrichedProfile::enrichWithTeacher);
            }
        }

        return Optional.of(enrichedProfile);
    }

    @Transactional(readOnly = true)
    public Optional<UserProfileDTO> getProfileByUserId(String userId) {
        return userProfileRepository.findByUserId(java.util.UUID.fromString(userId))
                .map(this::toDTO);
    }

    @Transactional(readOnly = true)
    public Optional<UserProfileDTO> getProfileByCurp(String curp) {
        return userProfileRepository.findByCurp(curp)
                .map(this::toDTO);
    }

    @Transactional(readOnly = true)
    public Optional<EnrichedProfileDTO> getEnrichedProfileByCurp(String curp) {
        Optional<User> userOpt = userRepository.findByCurp(curp);
        if (userOpt.isEmpty()) {
            return Optional.empty();
        }

        User user = userOpt.get();
        Set<String> roles = user.getRoles().stream()
                .map(role -> role.getName())
                .collect(Collectors.toSet());

        Optional<UserProfile> profileOpt = userProfileRepository.findByCurp(curp);
        UserProfileDTO profileDTO = profileOpt.map(this::toDTO).orElse(null);

        EnrichedProfileDTO enrichedProfile;
        if (profileDTO != null) {
            enrichedProfile = EnrichedProfileDTO.fromUserProfile(profileDTO, roles);
        } else {
            enrichedProfile = EnrichedProfileDTO.builder()
                    .curp(curp)
                    .roles(roles)
                    .build();
        }

        if (roles.contains("STUDENT")) {
            studentRepository.findByCurpAndIsDeletedFalse(curp)
                    .ifPresent(enrichedProfile::enrichWithStudent);
        }
        if (roles.contains("TEACHER")) {
            teacherRepository.findByCurpAndIsDeletedFalse(curp)
                    .ifPresent(enrichedProfile::enrichWithTeacher);
        }

        return Optional.of(enrichedProfile);
    }

    @Transactional(readOnly = true)
    public Object getAcademicHistory(String userId) {
        Optional<User> userOpt = userRepository.findById(java.util.UUID.fromString(userId));
        if (userOpt.isEmpty()) {
            return null;
        }

        User user = userOpt.get();
        // TODO: Implement academic history retrieval from enrollment, grades, etc.
        return java.util.Map.of(
                "userId", userId,
                "message", "Academic history endpoint - to be implemented"
        );
    }

    @Transactional
    public UserProfileDTO createOrUpdateProfile(String userId, UpdateProfileRequest request) {
        User user = userRepository.findById(java.util.UUID.fromString(userId))
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

        // Validate CURP format if provided
        if (request.getCurp() != null && !isValidCurp(request.getCurp())) {
            throw new IllegalArgumentException("CURP inválido. Debe tener 18 caracteres y formato válido");
        }

        // Validate RFC format if provided
        if (request.getRfc() != null && !isValidRfc(request.getRfc())) {
            throw new IllegalArgumentException("RFC inválido. Debe tener 13 caracteres y formato válido");
        }

        UserProfile profile = userProfileRepository.findByUserId(user.getId())
                .orElse(new UserProfile());

        if (profile.getId() == null) {
            profile.setUser(user);
        }

        if (request.getFirstName() != null) profile.setFirstName(request.getFirstName());
        if (request.getLastName() != null) profile.setLastName(request.getLastName());
        if (request.getCurp() != null) profile.setCurp(request.getCurp());
        if (request.getRfc() != null) profile.setRfc(request.getRfc());
        if (request.getPhone() != null) profile.setPhone(request.getPhone());
        if (request.getSecondaryPhone() != null) profile.setSecondaryPhone(request.getSecondaryPhone());
        if (request.getBirthDate() != null) profile.setBirthDate(request.getBirthDate());
        if (request.getGender() != null) profile.setGender(request.getGender());
        if (request.getEmployeeNumber() != null) profile.setEmployeeNumber(request.getEmployeeNumber());
        if (request.getEnrollmentNumber() != null) profile.setEnrollmentNumber(request.getEnrollmentNumber());
        if (request.getInstitutionalEmail() != null) profile.setInstitutionalEmail(request.getInstitutionalEmail());
        if (request.getSecondaryEmail() != null) profile.setSecondaryEmail(request.getSecondaryEmail());
        if (request.getAddress() != null) profile.setAddress(request.getAddress());
        if (request.getCity() != null) profile.setCity(request.getCity());
        if (request.getState() != null) profile.setState(request.getState());
        if (request.getPostalCode() != null) profile.setPostalCode(request.getPostalCode());
        if (request.getProfilePictureUrl() != null) profile.setProfilePictureUrl(request.getProfilePictureUrl());

        profile = userProfileRepository.save(profile);

        // Sync with Student/Teacher entities if user has those roles
        syncWithAcademicEntities(user, profile);

        return toDTO(profile);
    }

    private void syncWithAcademicEntities(User user, UserProfile profile) {
        Set<String> roles = user.getRoles().stream()
                .map(role -> role.getName())
                .collect(Collectors.toSet());

        if (roles.contains("STUDENT") && profile.getCurp() != null) {
            studentRepository.findByCurpAndIsDeletedFalse(profile.getCurp())
                    .ifPresent(student -> {
                        student.setFirstName(profile.getFirstName());
                        student.setLastName(profile.getLastName());
                        student.setPhone(profile.getPhone());
                        student.setInstitutionalEmail(profile.getInstitutionalEmail());
                        student.setBirthDate(profile.getBirthDate());
                        student.setGender(profile.getGender());
                        studentRepository.save(student);
                        log.info("Synced profile to student for curp: {}", profile.getCurp());
                    });
        }

        if (roles.contains("TEACHER") && profile.getCurp() != null) {
            teacherRepository.findByCurpAndIsDeletedFalse(profile.getCurp())
                    .ifPresent(teacher -> {
                        teacher.setFirstName(profile.getFirstName());
                        teacher.setLastName(profile.getLastName());
                        teacher.setRfc(profile.getRfc());
                        teacher.setPhone(profile.getPhone());
                        teacher.setInstitutionalEmail(profile.getInstitutionalEmail());
                        teacherRepository.save(teacher);
                        log.info("Synced profile to teacher for curp: {}", profile.getCurp());
                    });
        }
    }

    private boolean isValidCurp(String curp) {
        return curp != null && curp.matches("^[A-Z]{4}[0-9]{6}[A-Z]{6}[A-Z0-9]{2}$");
    }

    private boolean isValidRfc(String rfc) {
        return rfc != null && rfc.matches("^[A-Z]{3,4}[0-9]{6}[A-Z0-9]{3}$");
    }

    @Transactional
    public void deleteProfile(String userId) {
        userProfileRepository.findByUserId(java.util.UUID.fromString(userId))
                .ifPresent(profile -> {
                    profile.setIsDeleted(true);
                    userProfileRepository.save(profile);
                });
    }

    private UserProfileDTO toDTO(UserProfile profile) {
        return UserProfileDTO.builder()
                .id(profile.getId())
                .firstName(profile.getFirstName())
                .lastName(profile.getLastName())
                .curp(profile.getCurp())
                .rfc(profile.getRfc())
                .phone(profile.getPhone())
                .secondaryPhone(profile.getSecondaryPhone())
                .birthDate(profile.getBirthDate())
                .gender(profile.getGender())
                .employeeNumber(profile.getEmployeeNumber())
                .enrollmentNumber(profile.getEnrollmentNumber())
                .institutionalEmail(profile.getInstitutionalEmail())
                .secondaryEmail(profile.getSecondaryEmail())
                .address(profile.getAddress())
                .city(profile.getCity())
                .state(profile.getState())
                .postalCode(profile.getPostalCode())
                .profilePictureUrl(profile.getProfilePictureUrl())
                .isActive(profile.getIsActive())
                .isDeleted(profile.getIsDeleted())
                .createdAt(profile.getCreatedAt())
                .build();
    }
}
