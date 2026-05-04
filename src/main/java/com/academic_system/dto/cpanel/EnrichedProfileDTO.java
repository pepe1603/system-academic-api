package com.academic_system.dto.cpanel;

import com.academic_system.entity.postgres.Student;
import com.academic_system.entity.postgres.Teacher;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EnrichedProfileDTO {
    private UUID id;
    private String firstName;
    private String lastName;
    private String curp;
    private String rfc;
    private String phone;
    private String secondaryPhone;
    private LocalDate birthDate;
    private String gender;
    private String employeeNumber;
    private String enrollmentNumber;
    private String institutionalEmail;
    private String secondaryEmail;
    private String address;
    private String city;
    private String state;
    private String postalCode;
    private String profilePictureUrl;
    private Boolean isActive;
    private Boolean isDeleted;
    private LocalDateTime createdAt;

    // Academic data
    private Set<String> roles;
    private StudentAcademicInfo studentInfo;
    private TeacherAcademicInfo teacherInfo;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StudentAcademicInfo {
        private UUID studentId;
        private String enrollmentNumber;
        private LocalDate enrollmentDate;
        private UUID generationId;
        private Boolean isActive;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TeacherAcademicInfo {
        private UUID teacherId;
        private String employeeNumber;
        private String rfc;
        private Boolean isActive;
    }

    public static EnrichedProfileDTO fromUserProfile(UserProfileDTO profile, Set<String> roles) {
        return EnrichedProfileDTO.builder()
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
                .roles(roles)
                .build();
    }

    public void enrichWithStudent(Student student) {
        this.studentInfo = StudentAcademicInfo.builder()
                .studentId(student.getId())
                .enrollmentNumber(student.getEnrollmentNumber())
                .enrollmentDate(student.getEnrollmentDate())
                .generationId(student.getGenerationId())
                .isActive(student.getIsActive())
                .build();
    }

    public void enrichWithTeacher(Teacher teacher) {
        this.teacherInfo = TeacherAcademicInfo.builder()
                .teacherId(teacher.getId())
                .employeeNumber(teacher.getEmployeeNumber())
                .rfc(teacher.getRfc())
                .isActive(teacher.getIsActive())
                .build();
    }
}
