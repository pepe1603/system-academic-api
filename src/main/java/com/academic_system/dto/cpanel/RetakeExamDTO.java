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
public class RetakeExamDTO {
    private UUID id;
    private BigDecimal previousAverage;
    private String status;
    private Boolean isDeleted;
    private LocalDateTime createdAt;

    private UUID studentId;
    private String studentName;
    private String enrollmentNumber;

    private UUID courseId;
    private String courseCode;
    private String courseName;

    private UUID academicSemesterId;
    private String academicSemesterName;

    private UUID originSemesterId;
    private String originSemesterName;
}
