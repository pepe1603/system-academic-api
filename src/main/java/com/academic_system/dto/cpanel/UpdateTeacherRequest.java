package com.academic_system.dto.cpanel;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateTeacherRequest {
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
}
