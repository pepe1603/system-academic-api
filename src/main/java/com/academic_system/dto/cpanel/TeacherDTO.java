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
public class TeacherDTO {
    private UUID id;
    private UUID userId;
    private String employeeNumber;
    private String rfc;
    private String curp;
    private String firstName;
    private String lastName;
    private String institutionalEmail;
    private String secondaryEmail;
    private String phone;
    private String secondaryPhone;
    private Boolean isActive;
    private Boolean isDeleted;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
