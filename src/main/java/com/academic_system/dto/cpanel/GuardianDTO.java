package com.academic_system.dto.cpanel;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GuardianDTO {
    private UUID id;
    private UUID studentId;
    private String studentName;
    private String enrollmentNumber;
    private String fullName;
    private String relationship;
    private String curp;
    private String primaryPhone;
    private String secondaryPhone;
    private String email;
    private String occupation;
    private String company;
    private String address;
    private Boolean isEmergencyContact;
    private Boolean isActive;
    private Boolean isDeleted;
    private LocalDateTime createdAt;
}
