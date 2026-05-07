package com.academic_system.dto.cpanel;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateStudentRequest {
    private String curp;
    private String enrollmentNumber;
    private String firstName;
    private String lastName;
    private UUID generationId;
    private UUID userId;
    private String institutionalEmail;
    private String phone;
    private LocalDate birthDate;
    private String gender;
    private LocalDate enrollmentDate;
    private Boolean isActive;
}
