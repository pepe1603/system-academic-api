package com.academic_system.dto.cpanel;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileDTO {
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
}
