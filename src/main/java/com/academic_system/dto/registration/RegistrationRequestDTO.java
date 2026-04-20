package com.academic_system.dto.registration;

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
public class RegistrationRequestDTO {

    private UUID id;
    private String curp;
    private String email;
    private String status;
    private LocalDateTime requestedAt;
    private LocalDateTime processedAt;
    private String rejectionReason;

    private UUID studentId;
    private UUID teacherId;
    private String studentName;
    private String teacherName;
}