package com.academic_system.dto.cpanel;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AttendancePeriodDTO {
    private UUID id;
    private UUID enrollmentId;
    private UUID academicSemesterId;
    private Integer totalClasses;
    private Integer totalPresent;
    private Integer totalAbsent;
    private Integer totalJustified;
    private Integer totalLate;
    private BigDecimal attendancePercentage;
    private String attendanceStatus;
    private String observations;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private String studentName;
    private String enrollmentNumber;
    private String courseCode;
    private String courseName;
    private String semesterName;
}
