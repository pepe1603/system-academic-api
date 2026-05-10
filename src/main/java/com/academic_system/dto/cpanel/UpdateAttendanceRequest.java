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
public class UpdateAttendanceRequest {
    private String status;
    private String classTime;
    private String subjectCode;
    private String observations;
    private UUID justifiedBy;
    private LocalDate justificationDate;
}
