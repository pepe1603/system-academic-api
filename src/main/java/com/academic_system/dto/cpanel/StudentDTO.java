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
public class StudentDTO {
    private UUID id;
    private UUID userId;
    private String enrollmentNumber;
    private String curp;
    private String firstName;
    private String lastName;
    private String institutionalEmail;
    private String phone;
    private LocalDate birthDate;
    private String gender;
    private LocalDate enrollmentDate;
    private UUID generationId;
    private String generationName;
    private Boolean isActive;
    private Boolean isDeleted;
    private LocalDate createdAt;
}
